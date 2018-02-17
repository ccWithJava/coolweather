package com.example.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtility;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivty extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView updateTime;

    private TextView temperatureText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiIndex;

    private TextView pm25Index;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    public DrawerLayout drawerLayout;

    private Button navButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //初始化各种控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        updateTime = findViewById(R.id.update_time);
        temperatureText = findViewById(R.id.temperature_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiIndex = findViewById(R.id.aqi_index);
        pm25Index = findViewById(R.id.pm25_index);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        bingPicImg = findViewById(R.id.bing_pic_img);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout=findViewById(R.id.drawer_layout);
        navButton=findViewById(R.id.nav_button);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pref.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        String bingPic = pref.getString("bing_pic_img", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            requestImage();
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    private void requestImage() {
        String imageUrlString = "http://guolin.tech/api/bing_pic";
        HttpUtility.sendOkHttpRequest(imageUrlString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPicString = response.body().string();
                if (bingPicString != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivty.this).edit();
                            editor.putString("bing_pic_img", bingPicString);
                            editor.apply();
                            Glide.with(WeatherActivty.this)
                                    .load(bingPicString).into(bingPicImg);
                        }
                    });
                }
            }
        });
    }


    private void showWeatherInfo(Weather weather) {
        //显示天气信息
        String cityName = weather.basic.cityName;
        String updataTimeHM = weather.basic.update.updateTime.split(" ")[1];
        Log.d("updata", updataTimeHM);
        String tempareture = weather.now.temperature;
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        updateTime.setText("更新时间:" + updataTimeHM);
        temperatureText.setText(tempareture + "℃");
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dataText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max + "℃");
            minText.setText(forecast.temperature.min + "℃");
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiIndex.setText(weather.aqi.city.aqi);
            pm25Index.setText(weather.aqi.city.pm25);
        }
        if (weather.suggestion != null) {
            comfortText.setText("舒适度：" + weather.suggestion.confort.info);
            carWashText.setText("洗车指数：" + weather.suggestion.carWash.info);
            sportText.setText("运动建议：" + weather.suggestion.sport.info);
        }
        weatherLayout.setVisibility(View.VISIBLE);
    }

    public void requestWeather(final String weatherId) {
        //到网络上去访问天气信息
        String urlString = "http://guolin.tech/api/weather?cityid="
                + weatherId + "&key=fdd4597d7cb6461087b7d4133a8607ca";
        HttpUtility.sendOkHttpRequest(urlString, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {                //此方法是在子线程里面
                final String responseString = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseString);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivty.this)
                                    .edit();
                            editor.putString("weather", responseString);
                            editor.apply();
                            mWeatherId=weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivty.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
                requestImage();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivty.this, "错误：获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }
}
