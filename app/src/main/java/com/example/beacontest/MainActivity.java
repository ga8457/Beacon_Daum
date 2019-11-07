package com.example.beacontest;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private BeaconManager beaconManager;
    // 감지된 비콘들을 임시로 담을 리스트
    private List<Beacon> beaconList = new ArrayList<>();
    TextView textView;
    TextView count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위치 권한 설정
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 2);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        textView = (TextView) findViewById(R.id.textView);
        count = (TextView) findViewById(R.id.count);

        textView.setText("연동 대기중...");
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        // 비콘 탐지를 시도한다
        beaconManager.bind(this);
        handler.sendEmptyMessage(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //계속 메모리에 상주하고 있을 수 있으므로 unbind 시켜야함
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                }
            }

        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            for(Beacon beacon : beaconList){
                String count2 = "beacon 연동된 개수: "+beaconList.size();
                count.setText(count2);
                if(beaconList.size() == 0){
                    textView.setText("연동된 비콘이 없음");
                }else{
                    String beaconinfo = "UUID:" + beacon.getId1() + " \nMajor:" + beacon.getId2() + "\nMinor:" + beacon.getId3() + " \nDistance:" + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n"
                            + "연동된 비콘이름: " + beacon.getBluetoothName() +" \n" + "맥주소: " + beacon.getBluetoothAddress();

                    Log.d("beaconInfo", beacon.toString());
                    textView.setText(beaconinfo);
                }

            }

            handler.sendEmptyMessageDelayed(0, 1000); //1초마다 호출
        }
    };
}