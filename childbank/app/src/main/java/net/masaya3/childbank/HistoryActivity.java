package net.masaya3.childbank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import net.masaya3.childbank.data.MoneyHistoryInfo;
import net.masaya3.childbank.data.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by masaya3 on 2017/06/17.
 */

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mDateView;
    private TextView mValueView;
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        TextView unitView = (TextView)this.findViewById(R.id.unitView);
        unitView.setText(ShareConfig.getInstance().moneyInfo.unit);

        mList = (ListView)this.findViewById(R.id.historyView);

        mValueView = (TextView)this.findViewById(R.id.valueView);

        mDateView = (TextView)this.findViewById(R.id.dateView);
        mDateView.setVisibility(View.INVISIBLE);

        View nfcView = this.findViewById(R.id.nfc);
        nfcView.setOnClickListener(this);
        View sendView = this.findViewById(R.id.send);
        sendView.setOnClickListener(this);
        View outputView = this.findViewById(R.id.output);
        outputView.setOnClickListener(this);

        //Max
        new AsyncJsonLoader(new AsyncJsonLoader.AsyncCallback() {
            @Override
            public void preExecute() {

            }

            @Override
            public void postExecute(JSONObject result) {

                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");

                mDateView.setText(String.format(getString(R.string.history_lastdate), df.format(new Date())));
                mDateView.setVisibility(View.VISIBLE);

                if(result == null || !result.has("data")){
                    mValueView.setText("---");
                    return;
                }


                try {
                    JSONObject data = result.getJSONObject("data");
                    long point = 0;
                    if(data.has("point")) {
                        point = data.getLong("point");
                    }
                    NumberFormat nfNum = NumberFormat.getNumberInstance();
                    mValueView.setText(nfNum.format(point));


                } catch (JSONException e) {
                    e.printStackTrace();
                    mValueView.setText("---");
                }



            }

            @Override
            public void progressUpdate(int progress) {
            }

            @Override
            public void cancel() {
            }
        }).execute(getString(R.string.wallets_url) + ShareConfig.getInstance().userInfo.id);

        //History
        new AsyncJsonLoader(new AsyncJsonLoader.AsyncCallback() {
            @Override
            public void preExecute() {

            }

            @Override
            public void postExecute(JSONObject result) {

                if(result == null || !result.has("data")){
                    return;
                }

                JSONArray historys = null;
                try {
                    historys = result.getJSONArray("data");

                    List<MoneyHistoryInfo> moneyHistoryInfoList = new ArrayList<MoneyHistoryInfo>();
                    for(int i=0; i<historys.length(); i++){
                        JSONObject history = historys.getJSONObject(i);

                        if(!history.has("transaction") && !history.has("target_user")){
                            continue;
                        }

                        JSONObject transaction = history.getJSONObject("transaction");
                        JSONObject target_user = history.getJSONObject("target_user");


                        if(transaction.getInt("parent_id") != ShareConfig.getInstance().userInfo.id){
                            continue;
                        }

                        UserInfo user = new UserInfo();
                        MoneyHistoryInfo info = new MoneyHistoryInfo();
                        info.money = transaction.getLong("amount");

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                        // Date型変換
                        try {
                            info.time = sdf.parse(transaction.getString("created_at"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        info.type = MoneyHistoryInfo.HistoryType.valueOf(transaction.getInt("category"));
                        info.targetInfo = user;

                        user.id = target_user.getInt("id");
                        user.name = target_user.getString("name");


                        moneyHistoryInfoList.add(info);

                    }

                    mList.setAdapter(new HistoryAdapter(getApplicationContext(), R.layout.adapter_history, moneyHistoryInfoList));

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
        }).execute(getString(R.string.history_url) + ShareConfig.getInstance().userInfo.id);

    }

    private MoneyHistoryInfo getHistoryDemo(UserInfo userInfo){
        MoneyHistoryInfo info = new MoneyHistoryInfo();

        Random rand = new Random();
        info.money = rand.nextInt(10000) + 100;
        info.type = MoneyHistoryInfo.HistoryType.valueOf(rand.nextInt(4));
        info.time = new Date();
        info.targetInfo = userInfo;
        info.moneyInfo = ShareConfig.getInstance().moneyInfo;

        return info;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.nfc:
                changeActivity(new Intent(this, NFCActivity.class));
                break;
            case R.id.send:
                changeActivity(new Intent(this, SendActivity.class));
                break;
            case R.id.output:
                changeActivity(new Intent(this, InputActivity.class));
                break;
        }

    }

    private void changeActivity(Intent intent){
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
