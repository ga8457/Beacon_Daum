package com.example.beacontest;

import android.icu.util.Output;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class beaconInformation extends AsyncTask<String ,Void, String> {

    String sendMsg, receiveMsg = "error";
    @Override
    protected String doInBackground(String... strings) {
        try{
            String str;
            URL url = new URL(strings[0]);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            httpURLConnection.setRequestMethod("POST");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
            sendMsg = "msg="+strings[1];
            if(strings.length>2) {
                for (int i = 2; i < strings.length; i++) {
                    sendMsg += "$msg" + i + "=" + strings[i];
                }
            }
            outputStreamWriter.write(sendMsg);
            outputStreamWriter.flush();
            if(httpURLConnection.getResponseCode() == httpURLConnection.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
            }else {
                Log.i("통신 결과", httpURLConnection.getResponseCode()+"에러");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return receiveMsg;
    }
}