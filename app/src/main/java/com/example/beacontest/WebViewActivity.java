package com.example.beacontest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class WebViewActivity extends AppCompatActivity {
    private WebSettings webSettings;
    private WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webVIew);

        webView.setWebViewClient(new WebViewClient());
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        Intent intent = getIntent();

        String add = intent.getStringExtra("add");
        String mod = intent.getStringExtra("mod");
        if(add != null){
            webView.loadUrl("http://211.61.141.25/work2/beacon/write.php");

        }else if(mod != null) {
            /*Intent intent3 = getIntent();
            String uuid = intent3.getStringExtra("uuid");
            String major = intent3.getStringExtra("major");
            String minor = intent3.getStringExtra("minor");
            webView.loadUrl("http://211.61.141.25/work2/beacon/write.php?UUID="+uuid+"&Major="+major+"&Minor="+minor);*/
        }
        else{
            webView.loadUrl(MyApplication.url);
        }



        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void sendMsg(final String str){
                String msg = str;
                if(str.equals("success")){
                    Toast.makeText(WebViewActivity.this, "정상적으로 비콘이 등록되었습니다" ,Toast.LENGTH_LONG).show();
                    Log.i("ScriptCall","success");

                    MyApplication.beaconlist.clear();

                    try {
                        MyApplication.result = new beaconInformation().execute("App").get();

                        String[] array = MyApplication.result.split("&");

                        int count = Integer.parseInt(array[0]);
                        MyApplication.listcount = count;
                        int j=1;
                        for(int i = 0 ; i < count ; i++){
                            String temp[] = {array[j], array[j+1], array[j+2], array[j+3], array[j+4]};
                            j += 5;
                            MyApplication.beaconlist.add(temp);
                        }

                        Toast.makeText(getApplicationContext(),"DB 받아오기 성공",Toast.LENGTH_LONG).show();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                }else{
                    Toast.makeText(WebViewActivity.this, "비콘등록실패" ,Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }, "App");


    }

}