package com.example.khangnt.weatherclient.rest;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient1 {
    public static final String BASE_URL_SEARCH_CITY = "https://maps.googleapis.com/maps/api/";
    public static final String API_KEY_CITY = "AIzaSyBtua1GM9RA2bKjuX-oOE7OZ69kostfk3Q";
    private static Retrofit retrofit = null;


    public static Retrofit getClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL_SEARCH_CITY)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
