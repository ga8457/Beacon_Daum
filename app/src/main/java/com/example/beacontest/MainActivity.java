package com.example.beacontest;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    private BeaconManager beaconManager;
    private ArrayAdapter<String> arrayAdapter;
    private List<Beacon> beaconList = new ArrayList<>();
    private final Region region = new Region("uniqueld", null, null, null);
    private final String MAC_address = "D0:B5:C2:C7:90:47";
    private List<String> list = new ArrayList<String>();
    private ListView listView;
    private WebView webView;
    private String Url = "";

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);
        listView.setFocusable(false);

        webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true); //자바스크립트 허용
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "android");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(arrayAdapter);
        webView.setVisibility(View.GONE);

        //위치 권한 설정
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 2);



        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        // 비콘 탐지를 시도한다
        beaconManager.bind(MainActivity.this);

        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String word = list.get(position);
                if(word.equals(daum)){
                    dialog(word);
                }
                else if(word.equals(google)){
                    dialog(word);
                }
                else {
                    dialog(word);
                }
            }
        });*/

        //리스트 눌렀을 시 클릭이벤트
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String word = (String)listView.getItemAtPosition(position); //어느 아이템을 눌렀는지

                if(word.contains("10010")) {
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl(Url);
                }
                else if(word.contains("10002")) {
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl(Url);
                }
                else if(word.contains("10004")) {
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl(Url);
                }
                else if(word.contains("10000")) {
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl(Url);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            beaconManager.stopMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.unbind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    beaconList = (List<Beacon>) beacons;
                }
                else {
                    Log.d("1", "비콘 없음");
                }
            }
        });
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
                String info="";
                list.clear();
                arrayAdapter.notifyDataSetChanged();

            if (beaconList.size() > 0) {
                for (int i = 0; i < beaconList.size(); i++) {
                    Beacon beacon = beaconList.get(i);
                        info = i+1+"번 비콘"+"\n이름: "+beacon.getBluetoothName()+"\nUUID: " + beacon.getId1() + "\nMajor: " + beacon.getId2() + "\nMinor: " + beacon.getId3()+"\n거리: "+ String.format("%.3f\n",beacon.getDistance());
                        list.add(info);

                        Log.d("list", info);

                        /*if(beacon.getDistance() < 1.0) {
                            //list.add(google);
                        }*/
                        /*info = i+1+"번 비콘"+"\nUUID: " + beacon.getId1() + "\nMajor: " + beacon.getId2() + "\nMinor: " + beacon.getId3()+"\n거리: "+ String.format("%.3f\n",beacon.getDistance());
                        list.add(info);

                        /*if(beacon.getDistance() < 1.0){
                            //list.add(daum);
                        }*/

                }
            }
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    /*public void dialog(String msg){
        String daumSite = "https://daum.net";
        String googleSite = "https://google.com";

        if(msg.equals(daum)){
            url = daumSite;
        }
        else if(msg.equals(google)){
            url = googleSite;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(msg);
        builder.setMessage("확인을 누르시면 " + msg + "로 이동됩니다");
        builder.setIcon(R.drawable.bitcon);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }*/


    //액션바 생성
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                list.clear();
                beaconManager.bind(this);
                handler.sendEmptyMessage(0);
                webView.setVisibility(View.GONE);
                break;

            case R.id.menu:
                beaconList.clear();
                list.clear();
                arrayAdapter.notifyDataSetChanged();
                try {
                    beaconManager.stopRangingBeaconsInRegion(region);
                    beaconManager.stopMonitoringBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                beaconManager.unbind(this);
                break;

            case R.id.menu2:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("비콘 등록");
                builder.setMessage("비콘 정보를 등록하시겠습니까?");
                builder.setIcon(R.drawable.bitcon);
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "비콘 등록", Toast.LENGTH_SHORT).show();
                        webView.setVisibility(View.VISIBLE);
                        webView.loadUrl("http://211.61.141.25/work2/beacon/write.php");
                    }
                });

                builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();

            default:
                return false;
        }
        return false;
    }


    @Override
    public void onBackPressed(){ //뒤로가기 이벤트
        super.onBackPressed();

        if(webView.canGoBack()) { //뒤로 갈 곳이 있는 경우
            webView.goBack();
        }
        else {

        }
    }

    private class WebViewClientClass extends WebViewClient {//페이지 이동
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void webViewFinish(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "정상적으로 비콘정보가 등록되었습니다", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}