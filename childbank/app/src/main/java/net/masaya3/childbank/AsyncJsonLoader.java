package net.masaya3.childbank;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by masaya3 on 2017/06/18.
 */

public class AsyncJsonLoader extends AsyncTask<String, Integer, JSONObject> {
    public interface AsyncCallback {
        void preExecute();
        void postExecute(JSONObject result);
        void progressUpdate(int progress);
        void cancel();
    }

    private AsyncCallback mAsyncCallback = null;


    private JSONObject mArgs;
    public void setArgs(JSONObject args){
        mArgs = args;
    }


    public AsyncJsonLoader(AsyncCallback _asyncCallback) {
        mAsyncCallback = _asyncCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mAsyncCallback.preExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... _progress) {
        super.onProgressUpdate(_progress);
        mAsyncCallback.progressUpdate(_progress[0]);
    }

    @Override
    protected void onPostExecute(JSONObject _result) {
        super.onPostExecute(_result);
        mAsyncCallback.postExecute(_result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mAsyncCallback.cancel();
    }

    @Override
    protected JSONObject doInBackground(String... urls){
        HttpURLConnection urlCon;
        InputStream in;

        //Httpコネクションを確立し、URLを叩いて情報を取得
        try {
            urlCon = (HttpURLConnection) new URL(urls[0]).openConnection();
            if(mArgs == null) {
                urlCon.setRequestMethod("GET");
                urlCon.setDoInput(true);
                urlCon.connect();
            }
            else{
                urlCon.setRequestMethod("POST");

                urlCon.setInstanceFollowRedirects(false);
                urlCon.setRequestProperty("Accept-Language", "jp");
                urlCon.setDoOutput(true);
                urlCon.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                OutputStream os = urlCon.getOutputStream();
                PrintStream ps = new PrintStream(os);
                ps.print(mArgs);
                ps.close();
            }

            String str_json = new String();
            in = urlCon.getInputStream();
            InputStreamReader objReader = new InputStreamReader(in);
            BufferedReader objBuf = new BufferedReader(objReader);
            StringBuilder strBuilder = new StringBuilder();
            String sLine;
            while((sLine = objBuf.readLine()) != null){
                strBuilder.append(sLine);
            }

            str_json = strBuilder.toString();
            in.close();

            try {
                return new JSONObject(str_json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
