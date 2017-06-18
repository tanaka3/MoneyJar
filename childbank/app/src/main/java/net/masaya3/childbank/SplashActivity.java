package net.masaya3.childbank;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import net.masaya3.childbank.data.MoneyInfo;

public class SplashActivity extends Activity {

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        Intent intent  = new Intent(SplashActivity.this, HistoryActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        }
    };


    /**
     *
     */
    @Override
    public void onResume(){
        super.onResume();
        mHandler.postDelayed(mRunnable, 2000);
    }

    /**
     *
     */
    @Override
    public void onPause(){
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }
}
