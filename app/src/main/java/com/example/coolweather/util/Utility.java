package com.example.coolweather.util;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by cc on 2018/2/14.
 */

public class Utility {

    public static boolean handleProvinceResponse(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Province province = new Province();
                province.setProvinceName(jsonObject.getString("name"));
                province.setProvinceCode(jsonObject.getInt("id"));
                province.save();
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                City city = new City();
                city.setCityName(jsonObject.getString("name"));
                city.setCityCode(jsonObject.getInt("id"));
                city.setProvinceId(provinceId);
                city.save();
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                County county = new County();
                county.setCountyName(jsonObject.getString("name"));
                county.setWeatherId(jsonObject.getString("weather_id"));
                county.setCityId(cityId);
                county.save();
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

