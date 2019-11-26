package com.example.beacontest;

import android.app.Application;

import java.util.ArrayList;

public class MyApplication extends Application {
    public static String result;
    public static ArrayList<String[]> beaconlist = new ArrayList<String[]>();
    public static int listcount;
    public static String url;
    @Override
    public void onCreate(){
        super.onCreate();
    }
}
