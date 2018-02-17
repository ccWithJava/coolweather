package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cc on 2018/2/16.
 */

public class Suggestion {

    @SerializedName("comf")
    public Confort confort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;


    public class Confort {

        @SerializedName("txt")
        public String info;

    }

    public class CarWash{

        @SerializedName("txt")
        public String info;

    }

    public class Sport{

        @SerializedName("txt")
        public String info;
    }
}
