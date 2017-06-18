package net.masaya3.childbank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

/**
 * Created by masaya3 on 2017/06/17.
 */

public class NFCActivity extends AppCompatActivity implements View.OnClickListener {
    private long mMoney;
    private TextView mValueView;
    private String mSendMoney = "0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

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

        mMoney = 1000;

        mValueView = (TextView)this.findViewById(R.id.valueView);
        setMoney();

        this.findViewById(R.id.history).setOnClickListener(this);
        this.findViewById(R.id.send).setOnClickListener(this);
        this.findViewById(R.id.output).setOnClickListener(this);
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

        Intent intent  = null;
        switch (v.getId()){
            case R.id.history:
                changeActivity(new Intent(this, HistoryActivity.class));
                break;
            case R.id.send:
                changeActivity(new Intent(this, SendActivity.class));
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
                    long point = Long.parseLong(mSendMoney);
                    startActivity(ChargeActivity.getIntent(this, point));
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
