package net.masaya3.childbank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.masaya3.childbank.data.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by masaya3 on 2017/06/17.
 */

public class SendActivity extends AppCompatActivity implements View.OnClickListener {

    private long mMoney;
    private TextView mValueView;
    private String mSendMoney = "0";
    private List<UserInfo> mUserList = new ArrayList<UserInfo>();
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        TextView unitView = (TextView)this.findViewById(R.id.unitView);
        unitView.setText(ShareConfig.getInstance().moneyInfo.unit);

        mSpinner = (Spinner)this.findViewById(R.id.target);

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

        mMoney = 1000;

        mValueView = (TextView)this.findViewById(R.id.valueView);
        setMoney();

        this.findViewById(R.id.nfc).setOnClickListener(this);
        this.findViewById(R.id.history).setOnClickListener(this);
        this.findViewById(R.id.output).setOnClickListener(this);

        //Max
        new AsyncJsonLoader(new AsyncJsonLoader.AsyncCallback() {
            @Override
            public void preExecute() {

            }

            @Override
            public void postExecute(JSONObject result) {

                if(result == null || !result.has("users")){
                    return;
                }


                try {
                    JSONArray array = result.getJSONArray("users");

                    List<String> names = new ArrayList<String>();
                    for(int i=0; i<array.length(); i++){
                        JSONObject usr = array.getJSONObject(i);

                        if(!usr.has("id")){
                            continue;
                        }

                        UserInfo info = new UserInfo();
                        info.id = usr.getInt("id");
                        info.name = usr.getString("name");

                        if(info.id == ShareConfig.getInstance().userInfo.id || info.id  == 0){
                            continue;
                        }
                        names.add(info.name);
                        mUserList.add(info);

                    }
                    ArrayAdapter<String> adapter
                            = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, names);

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // spinner に adapter をセット
                    mSpinner.setAdapter(adapter);
                    mSpinner.setSelection(0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void progressUpdate(int progress) {
            }

            @Override
            public void cancel() {
            }
        }).execute(getString(R.string.users_url));



        //Max
        new AsyncJsonLoader(new AsyncJsonLoader.AsyncCallback() {
            @Override
            public void preExecute() {

            }

            @Override
            public void postExecute(JSONObject result) {

                if(result == null || !result.has("data")){
                    return;
                }


                try {
                    JSONObject data = result.getJSONObject("data");
                    long point = 0;
                    if(data.has("point")) {
                        mMoney = data.getLong("point");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void progressUpdate(int progress) {
            }

            @Override
            public void cancel() {
            }
        }).execute(getString(R.string.wallets_url) + ShareConfig.getInstance().userInfo.id);

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
        if(money > mMoney){
            mSendMoney = String.valueOf(mMoney);
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
            case R.id.output:
                changeActivity(new Intent(this, InputActivity.class));
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


                    UserInfo info = mUserList.get(mSpinner.getSelectedItemPosition());

                    JSONObject post = new JSONObject();
                    post.put("parentId", ShareConfig.getInstance().userInfo.id);
                    post.put("childId", info.id);
                    post.put("amount", amount);

                    //Max
                    AsyncJsonLoader loader =  new AsyncJsonLoader(new AsyncJsonLoader.AsyncCallback() {
                        @Override
                        public void preExecute() {

                        }

                        @Override
                        public void postExecute(JSONObject result) {

                            if(result == null || !result.has("message")){
                                Toast.makeText(getApplicationContext(), "送金に失敗しました", Toast.LENGTH_LONG).show();
                                return;
                            }

                            try {
                                if(result.getString("message").equals("Success")){
                                    Toast.makeText(getApplicationContext(), "送金しました", Toast.LENGTH_LONG).show();
                                    changeActivity(new Intent(SendActivity.this, HistoryActivity.class));
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(), "送金に失敗しました", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void progressUpdate(int progress) {
                        }

                        @Override
                        public void cancel() {
                        }
                    });
                    loader.setArgs(post);
                    loader.execute(getString(R.string.execute_url));

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
