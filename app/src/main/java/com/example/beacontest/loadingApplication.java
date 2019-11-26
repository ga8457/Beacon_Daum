package com.example.beacontest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class loadingApplication extends Activity {

    @Override
    protected void onCreate(Bundle savedlnstanceState) {
        super.onCreate(savedlnstanceState);

        MyApplication.beaconlist.clear();

        try {
            MyApplication.result = new beaconInformation().execute("http://211.61.141.25/work2/beacon/beaconList.php", "get_list").get();

            String[] array = MyApplication.result.split("&");

            int count = Integer.parseInt(array[0]);
            MyApplication.listcount = count;

            for(int i = 0, j = 1; i < count ; i++, j+=5){
                String temp[] = {array[j], array[j+1], array[j+2], array[j+3], array[j+4]};
                MyApplication.beaconlist.add(temp);
            }

            Toast.makeText(getApplicationContext(), "DB 받아오기 완료", Toast.LENGTH_SHORT).show();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();

    }
}
