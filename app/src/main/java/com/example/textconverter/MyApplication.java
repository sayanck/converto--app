package com.example.textconverter;

import android.app.Application;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static String formatTimestamp(long timestamp){

        Calendar calender= Calendar.getInstance(Locale.ENGLISH);
        calender.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy",calender).toString();
        return date;
    }

}
