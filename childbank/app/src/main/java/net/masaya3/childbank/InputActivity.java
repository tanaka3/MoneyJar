package net.masaya3.childbank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class InputActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mValueView;
    private String mSendMoney = "0";
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        TextView unitView = (TextView)this.findViewById(R.id.unitView);
        unitView.setText(ShareConfig.getInstance().moneyInfo.unit);

        this.findViewById(R.id.button0).setOnClickListener(this);
        this.findViewById(R.id.button1).setOnClickListener(this);
        this.findViewById(R.id.button2).setOnClickListener(this);
        this.findViewById(R.id.button3).setOnClickListener(this);
        this.findViewById(R.id.button4).setOnClickListener(this);
        this.findViewById(R.id.button5).setOnClickListener(this);
        this.findViewById(R.id.button6).setOnClickListener(this);
        this.findViewById(R.id.button7).setOnClickListener(this);
        this.findViewById(R.id.button8).setOnClickListener(this);
        this.findViewById(R.id.button9).setOnClickListener(this);
        this.findViewById(R.id.button0).setOnClickListener(this);
        this.findViewById(R.id.buttonBack).setOnClickListener(this);
        this.findViewById(R.id.buttonSend).setOnClickListener(this);

        mValueView = (TextView)this.findViewById(R.id.valueView);
        setMoney();

        View nfcView = this.findViewById(R.id.nfc);
        nfcView.setOnClickListener(this);
        View sendView = this.findViewById(R.id.send);
        sendView.setOnClickListener(this);
        View outputView = this.findViewById(R.id.history);
        outputView.setOnClickListener(this);
    }

    private void setMoney(){
        long money = Long.parseLong(mSendMoney);
        NumberFormat nfNum = NumberFormat.getNumberInstance();
        mValueView.setText(nfNum.format(money));
    }

    private void addMoney(String num){

        if(mSendMoney.equals("0")){
            mSendMoney = num;
        }
        else {
            mSendMoney += num;
        }

        long money = Long.parseLong(mSendMoney);
        if(money > 999999){
            mSendMoney = String.valueOf(999999);
        }
    }

    private void backMoney(){
        if(mSendMoney.length() < 2){
            mSendMoney = "0";
            return;
        }
        mSendMoney = mSendMoney.substring(0, mSendMoney.length()-1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.history:
                changeActivity(new Intent(this, HistoryActivity.class));
                break;
            case R.id.nfc:
                changeActivity(new Intent(this, NFCActivity.class));
                break;
            case R.id.send:
                changeActivity(new Intent(this, SendActivity.class));
                break;
            case R.id.buttonBack:
                backMoney();
                setMoney();
                break;
            case R.id.buttonSend:

                if(mSendMoney.equals("0")){
                    return;
                }

                try {
                    long amount = Long.parseLong(mSendMoney);


                    JSONObject post = new JSONObject();
                    post.put("parentId", ShareConfig.getInstance().userInfo.id);
                    post.put("amount", amount);

                    //Max
                    AsyncJsonLoader loader =  new AsyncJsonLoader(new AsyncJsonLoader.AsyncCallback() {
                        @Override
                        public void preExecute() {

                        }

                        @Override
                        public void postExecute(JSONObject result) {

                            if(result == null || !result.has("message")){
                                Toast.makeText(getApplicationContext(), "入金に失敗しました", Toast.LENGTH_LONG).show();
                                return;
                            }

                            try {
                                if(result.getString("message").equals("Success")){
                                    Toast.makeText(getApplicationContext(), "入金しました", Toast.LENGTH_LONG).show();
                                    changeActivity(new Intent(InputActivity.this, HistoryActivity.class));
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(), "入金に失敗しました", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void progressUpdate(int progress) {
                        }

                        @Override
                        public void cancel() {
                        }
                    });
                    loader.setArgs(post);
                    loader.execute(getString(R.string.input_url));

                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                addMoney(((TextView)v).getText().toString());
                setMoney();
                break;
        }

    }

    private void changeActivity(Intent intent){
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
