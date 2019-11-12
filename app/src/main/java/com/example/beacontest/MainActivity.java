package com.example.beacontest;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위치 권한 설정
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 2);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        textView = (TextView) findViewById(R.id.textView);
        Button button = (Button)findViewById(R.id.button);

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        // 비콘 탐지를 시도한다
        beaconManager.bind(MainActivity.this);
        handler.sendEmptyMessage(0);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("주변에 비콘을 탐색 중입니다...");
                findViewById(R.id.button).setVisibility(View.INVISIBLE);
                beaconManager.bind(MainActivity.this);
                handler.sendEmptyMessage(0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        textView.setText("기존 비콘이 삭제되었습니다. \n재연동 버튼을 눌러주세요");
    }

    @Override
                public void onBeaconServiceConnect() {
                    beaconManager.setRangeNotifier(new RangeNotifier() {
                        @Override
                        // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
                        // region에는 비콘들에 대응하는 Region 객체가 들어온다.
                        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                            if (beacons.size() > 0) {
                                for (Beacon beacon : beacons) {
                                    beaconList.add(beacon);
                                }
                            }
            }

        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("uniqueld", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("uniqueld", null,null,null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {

                for (Beacon beacon:beaconList) {
                        String beaconinfo = "UUID: " + beacon.getId1() + " \nMajor: " + beacon.getId2() + "\nMinor: " + beacon.getId3() + " \n비콘과의 떨어진 거리: " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n"
                                + "연동된 비콘이름: " + beacon.getBluetoothName() + " \n맥주소: " + beacon.getBluetoothAddress() + "\n신호량: " + beacon.getRssi();

                    if (beacon.getDistance() < 1.0) {
                        Toast.makeText(MainActivity.this, "비콘이 주변에 있습니다" + "("+ Double.parseDouble(String.format("%.3f", beacon.getDistance()))+"m)", Toast.LENGTH_SHORT).show();
                        beaconManager.unbind(MainActivity.this);
                        daum_handler.sendEmptyMessage(0);

                    } else {
                        Log.d("Distance", "1m 이상");
                    }

                    Log.d("beaconInfo", beacon.toString());
                    textView.setText(beaconinfo);
                }
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    Handler daum_handler = new Handler(){
        public void handleMessage(Message msg) {
           try {
               beaconManager.unbind(MainActivity.this);
               beaconList.clear();
           } catch(Exception e) {
               e.printStackTrace();
           }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.daum.net"));
            startActivity(intent);
            findViewById(R.id.button).setVisibility(View.VISIBLE);
        }
    };

}