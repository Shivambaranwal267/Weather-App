package com.example.weatherapp.utils;

import android.Manifest;

public class Constants {

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String nameOfSharedPreferences = "com.example.weatherapp";
    public static final String keyForDeniedAllPermissionCount = "deniedAllPermissionCount";
    public static final String keyForDeniedOnlyPermissionCount = "deniedOnlyPermissionCount";
    public static final String intentName = "weather";
    public static final String byCityName = "By city name";
    public static final String byLocation = "By location";
    public static final String BASE_URL = "https://api.openweathermap.org/";
    public static final String SUB_URL = "/data/2.5/weather?appid=f247e252b56aea820fc0cb7527712490&units=metric";
    //api.openweathermap.org/data/2.5/weather?q=meerut&appid=56fa4ed8f2a9a62b4066f3ad70ac5bbf


}
