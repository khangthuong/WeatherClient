package com.example.khangnt.weatherclient.rest;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface ApiInterface {
    //@GET("locations/v1/cities/search")
    //Call<ResponseBody> getLocationKey(@Query("apikey") String apiKey, @Query("q") String city);

    @GET("{key}/{latitude},{longitude}?exclude=minutely,hourly,daily,flags")
    Call<ResponseBody> getCurentCondition(@Path("key") String key,
                                          @Path("latitude") double latitude,
                                          @Path("longitude") double longitude);

    @GET("{key}/{latitude},{longitude}?exclude=minutely,currently,daily,flags")
    Call<ResponseBody> getHourlyCondition(@Path("key") String key,
                                          @Path("latitude") double latitude,
                                          @Path("longitude") double longitude);

    @GET("{key}/{latitude},{longitude}?exclude=minutely,currently,hourly,flags")
    Call<ResponseBody> getDailyCondition(@Path("key") String key,
                                          @Path("latitude") double latitude,
                                          @Path("longitude") double longitude);

}
