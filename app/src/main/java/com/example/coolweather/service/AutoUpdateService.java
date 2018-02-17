package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtility;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager alarmManager=(AlarmManager) getSystemService(ALARM_SERVICE);
        int eightHour=8*60*60*1000;
        long triggerAtTime= SystemClock.elapsedRealtime()+eightHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherResponse = pref.getString("weather", null);
        if (weatherResponse != null) {
            Weather weather = Utility.handleWeatherResponse(weatherResponse);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid="
                    + weatherId + "&key=fdd4597d7cb6461087b7d4133a8607ca";
            HttpUtility.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherResponse = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weather", weatherResponse);
                    editor.apply();
                }
            });
        }
    }

    private void updateBingPic() {
        String requestBingPicUrl = "http://guolin.tech/api/bing_pic";
        HttpUtility.sendOkHttpRequest(requestBingPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                SharedPreferences.Editor editor=PreferenceManager
                        .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic_img",responseString);
                editor.apply();
            }
        });
    }
}
