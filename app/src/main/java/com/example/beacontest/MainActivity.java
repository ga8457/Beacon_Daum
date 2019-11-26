package com.example.beacontest;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    private long backTime;
    private Toast toast;
    private List<String> resultList = new ArrayList<String>();
    private TextView textView;
    private int search = 1;
    private int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위치 권한 설정
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 2);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        // 비콘 탐지를 시도한다
        //beaconManager.bind(MainActivity.this);

        textView = (TextView) findViewById(R.id.textView);
        //gPHP.execute(url);


        listView = (ListView) findViewById(R.id.listView);
        listView.setVisibility(View.GONE);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultList);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = resultList.get(position);
                String arrays[] = temp.split("\n");
                String name = arrays[1].split(":")[1];
                dialog(name, arrays);
            }
        });
    }

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
                } else {
                    Log.d("1", "비콘 없음");
                }
            }
        });
    }

    public void beaconscanStart() {
        Log.d("end_", "시작");
        beaconManager.bind(MainActivity.this);
        Toast.makeText(getApplicationContext(), "5초 간 비콘스캔을 시작합니다", Toast.LENGTH_SHORT).show();

        new Thread() {
            public void run() {
                int num = 0;
                while (num < 5) {
                    handler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    num++;
                }
                beaconscanStop();
            }
        }.start();
    }

    public void beaconscanStop() {
        Log.d("end_", "끝");
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            beaconManager.stopMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.unbind(this);
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String info;
            list.clear();

            if (beaconList.size() > 0) {
                for (int i = 0; i < beaconList.size(); i++) {
                    Beacon beacon = beaconList.get(i);
                    info ="이름: " + beacon.getBluetoothName() + "\nUUID:" + beacon.getId1() + "\nMajor:" + beacon.getId2() + "\nMinor:" + beacon.getId3();
                        /*if(beacon.getDistance() < 1.0) {
                            //list.add(google);
                        }*/
                    list.add(info);
                    arrayAdapter.notifyDataSetChanged();
                }
                //10002 54478 cc

                for (int i = 0; i < list.size(); i++) {
                    if (!resultList.contains(list.get(i))) {
                        resultList.add(list.get(i));
                    }
                }
                arrayAdapter.notifyDataSetInvalidated();
                list.clear();
            }
        }
    };

    public void dialog(String name, String arrays[]) {
        String UUID = arrays[1].split(":")[1];
        String Major = arrays[2].split(":")[1];
        String Minor = arrays[3].split(":")[1];
        String url = null;

        for (int i = 0; i < MyApplication.listcount; i++) {
            String[] temp_list = MyApplication.beaconlist.get(i);
            if (UUID.equals(temp_list[0]) && Major.equals(temp_list[1]) && Minor.equals(temp_list[2]))
            {
                url = temp_list[4];
            }
        }
                        String finalUrl = url;
                        if (finalUrl != null) {
                            MyApplication.url = finalUrl;
                            // intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                            Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "비콘을 등록해주세요", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), EditActivity.class);

                            intent.putExtra("UUID", UUID);
                            intent.putExtra("Major", Major);
                            intent.putExtra("Minor", Minor);
                            intent.putExtra("mod", "mod");
                            startActivity(intent);
                        }
    }


    //액션바 생성
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search) {
            beaconList.clear();
            arrayAdapter.notifyDataSetChanged();
            listView.setVisibility(View.VISIBLE);
            beaconscanStart();
            search = 0;
        }
        else if (id == R.id.menu) {
            resultList.clear();
            arrayAdapter.notifyDataSetChanged();
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            beaconManager.unbind(this);

        } else if (id == R.id.menu2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("비콘 등록");
            builder.setMessage("비콘 정보를 등록하시겠습니까?");
            builder.setIcon(R.drawable.bitcon);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent Add_Intent = new Intent(MainActivity.this, EditActivity.class);
                    Add_Intent.putExtra("add", "add");
                    startActivity(Add_Intent);
                }
            });

            builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //아무 반응 없음
                }
            });
            builder.create().show();
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backTime + 1500) {
            backTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if (System.currentTimeMillis() <= backTime + 1500) {
            finish();
        }
    }


    /*private class WebViewClientClass extends WebViewClient {//페이지 이동
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }
    }*/
}