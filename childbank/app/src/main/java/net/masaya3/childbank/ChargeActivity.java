package net.masaya3.childbank;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import net.masaya3.childbank.data.NFCPointInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Created by masaya3 on 2017/06/17.
 */

public class ChargeActivity extends AppCompatActivity {

    public static String ARGS_POINT = "point";

    private NFCPointInfo mPointInfo;
    private IntentFilter[] mIntentFiltersArray;
    private String[][] mTechListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private long mChargePoint = 0;
    private boolean isWriteEnd = false;

    public static Intent getIntent(Context context, long point){
        Intent intent = new Intent(context, ChargeActivity.class);
        intent.putExtra(ARGS_POINT, point);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);

        Intent intent = getIntent();

        if(intent.hasExtra(ARGS_POINT)) {
            mChargePoint = intent.getLongExtra(ARGS_POINT, 0);
        }

        mPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter[] intentFilter = new IntentFilter[] {
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        };
        String[][] techList = new String[][] {
                {android.nfc.tech.NdefFormatable.class.getName()},
                {android.nfc.tech.Ndef.class.getName()}
        };
        // NfcAdapterを取得
        mAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // NFCの読み込みを有効化
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFiltersArray, mTechListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if(isWriteEnd){
           return;
        }
        super.onNewIntent(intent);
        String action = intent.getAction();

        Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null && mPointInfo != null) {
            Toast.makeText(getApplicationContext(), "対象のカードではありません", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> techList = Arrays.asList(tag.getTechList());

        NFCPointInfo  point = null;

        //アップデート時
        if(mPointInfo != null) {
            Ndef ndef = Ndef.get(tag);
            NdefMessage message = null;

            if (ndef != null) {
                message = ndef.getCachedNdefMessage();


                NdefRecord[] records = null;
                if (message != null) {
                    records = message.getRecords();
                }

                if (records == null || records.length == 0) {
                    return;
                }

                for (NdefRecord record : records) {


                    JSONObject json = null;
                    try {
                        String json_str = getText(record);

                        json = new JSONObject(json_str);
                        point = NFCPointInfo.parse(json);
                        break;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                point = new NFCPointInfo(0);
            }

            point.mPoint += mChargePoint;
        }
        //新規の場合
        else{
            point = new NFCPointInfo(mChargePoint);
        }

        NdefMessage message = createTextMessage(false, "en",point.toJSONObject().toString());

        boolean hasWrite = false;
        if (techList.contains(Ndef.class.getName())) {
            Ndef ndef = Ndef.get(tag);
            hasWrite = writeNdefToNdefTag(ndef, message);
        } else if (techList.contains(NdefFormatable.class.getName())) {
            NdefFormatable ndef = NdefFormatable.get(tag);
            hasWrite = writeNdefToNdefFormatable(ndef, message);
        } else {
            Toast.makeText(getApplicationContext(), "書き込みに失敗しました", Toast.LENGTH_LONG).show();
        }

        if(hasWrite){
            isWriteEnd = true;
            JSONObject post = new JSONObject();
            try {
                post.put("parentId", ShareConfig.getInstance().userInfo.id);
                post.put("childId", 0);
                post.put("amount", mChargePoint);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Max
            AsyncJsonLoader loader =  new AsyncJsonLoader(new AsyncJsonLoader.AsyncCallback() {
                @Override
                public void preExecute() {

                }

                @Override
                public void postExecute(JSONObject result) {

                    if(result == null || !result.has("message")){
                        Toast.makeText(getApplicationContext(), "書き込みに失敗しました", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        if(result.getString("message").equals("Success")){
                            Toast.makeText(getApplicationContext(), "書き込みが完了しました。", Toast.LENGTH_LONG).show();
                            changeActivity(new Intent(ChargeActivity.this, HistoryActivity.class));
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "書き込みに失敗しました", Toast.LENGTH_LONG).show();
                }

                @Override
                public void progressUpdate(int progress) {
                }

                @Override
                public void cancel() {
                }
            });
            loader.setArgs(post);
            loader.execute(getString(R.string.charge_url));
        }
    }
    /**
     * IDmを取得する
     * @param intent
     * @return
     */
    private String getIdm(Intent intent) {
        String idm = null;
        StringBuffer idmByte = new StringBuffer();
        byte[] rawIdm = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        if (rawIdm != null) {
            for (int i = 0; i < rawIdm.length; i++) {
                idmByte.append(Integer.toHexString(rawIdm[i] & 0xff));
            }
            idm = idmByte.toString();
        }
        return idm;
    }

    private NdefMessage createTextMessage(boolean isUtf8, String lang, String text) {

        Charset utfType = isUtf8 ? Charset.forName("UTF-8"):Charset.forName("UTF-16");
        byte[] langCode = lang.getBytes();
        byte[] textData = text.getBytes();
        byte[] status = new byte[] {(byte)( (isUtf8 ? 0:(1<<7)) + langCode.length & 0x3f)};
        // Payloadのデータ組み立て
        byte[] payload = new byte[status.length + langCode.length + textData.length];
        System.arraycopy(status, 0, payload, 0, status.length);
        System.arraycopy(langCode, 0, payload, status.length, langCode.length);
        System.arraycopy(textData, 0, payload, status.length + langCode.length, textData.length);
        NdefMessage message = new NdefMessage(new NdefRecord[] {new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[]{}, payload)});
        return message;

    }

    private boolean writeNdefToNdefTag(Ndef ndef, NdefMessage message) {
        int size = message.toByteArray().length;

        try {
            ndef.connect();
            if (!ndef.isWritable()) {
                return false;
            }
            if (ndef.getMaxSize() < size) {
                return false;
            }

            ndef.writeNdefMessage(message);
            return true;

        } catch (IOException e) {
            //throw new RuntimeException(e);
        } catch (FormatException e) {
            //throw new RuntimeException(e);
        } finally {
            try {
                ndef.close();
            } catch (IOException e) {
                // ignore
            }
        }

        return false;
    }

    private boolean writeNdefToNdefFormatable(NdefFormatable ndef, NdefMessage message) {
        try {
            ndef.connect();
            ndef.format(message);

            return true;
        } catch (IOException e) {
            //throw new RuntimeException(e);
        } catch (FormatException e) {
            //throw new RuntimeException(e);
        } finally {
            try {
                ndef.close();
            } catch (IOException e) {
                // ignore
            }
        }

        return false;
    }

    private boolean isTextRecord(NdefRecord record) {
        return record.getTnf() == NdefRecord.TNF_WELL_KNOWN
                && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT);
    }

    private String getText(NdefRecord record) {
        if (record == null || !isTextRecord(record)) {
            return "";
        }

        byte[] payload = record.getPayload();
        byte flags = payload[0];
        String encoding = ((flags & 0x80) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = flags & 0x3F;
        try {
            return new String(payload, 1 + languageCodeLength, payload.length
                    - (1 + languageCodeLength), encoding);
        } catch (UnsupportedEncodingException e) {
        } catch (IndexOutOfBoundsException e) {
        }

        return "";
    }

    private void changeActivity(Intent intent){
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
