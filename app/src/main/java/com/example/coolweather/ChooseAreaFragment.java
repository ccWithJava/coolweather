package com.example.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtility;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by cc on 2018/2/14.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private List<String> dataList = new ArrayList<>();

    private Province selectedProvince;
    private City selectedCity;

    private ListView listView;
    private TextView titleText;
    private Button backButton;

    private ArrayAdapter<String> adapter;

    private int currentLevel;

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(currentLevel){
                    case LEVEL_CITY:
                        queryProvince();
                        break;
                    case LEVEL_COUNTY:
                        queryCity();
                        break;
                    default:
                        break;
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        selectedProvince=provinceList.get(position);
                        queryCity();
                        break;
                    case LEVEL_CITY:
                        selectedCity=cityList.get(position);
                        queryCounty();
                        break;
                    default:
                        break;
                }
            }
        });
        queryProvince();

    }

    private void queryProvince() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            currentLevel=LEVEL_PROVINCE;
        }
        else{
            //到网络上去访问数据 http://guolin.tech/api/china/
            String address="http://guolin.tech/api/china/";
            queryFromServer(address,"province");
        }
    }

    private void queryCity(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceId=?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            currentLevel=LEVEL_CITY;
        }
        else{
            String address="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/";
            queryFromServer(address,"city");
        }

    }

    private void queryCounty(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityId=?",
                String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            currentLevel=LEVEL_COUNTY;
        }
        else{
            String address="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()
                    +"/"+selectedCity.getCityCode()+"/";
            queryFromServer(address,"county");
        }
    }


    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtility.sendHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                Toast.makeText(getContext(),"加载失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseFromServer=response.body().string();
                boolean isSuccessful=false;
                if(type.equals("province")){
                    isSuccessful=Utility.handleProvinceResponse(responseFromServer);
                }
                else if(type.equals("city")){
                    isSuccessful=Utility.handleCityResponse(responseFromServer,
                            selectedProvince.getId());
                }
                else if(type.equals("county")){
                    isSuccessful=Utility.handleCountyResponse(responseFromServer,
                            selectedCity.getId());
                }
                if(isSuccessful){
                    getActivity().runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    if(type.equals("province")){
                                        queryProvince();
                                    }
                                    else if(type.equals("city")){
                                        queryCity();
                                    }
                                    else if(type.equals("county")) {
                                        queryCounty();
                                    }
                                }
                            }
                    );
                }
            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
