package com.example.khangnt.weatherclient.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.khangnt.weatherclient.activity.WeatherActivity;
import com.example.khangnt.weatherclient.model.WeatherDataCurrent;
import com.example.khangnt.weatherclient.rest.ApiClient;
import com.example.khangnt.weatherclient.rest.ApiInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LOCATION_SERVICE;
import static com.example.khangnt.weatherclient.rest.ApiClient.API_KEY;

public class AlarmReceiver extends BroadcastReceiver {

    private final String TAG = AlarmReceiver.class.getSimpleName();
    private Context context;
    private Activity current_activity;
    private NotificationUtils notificationUtils;
    private FusedLocationProviderClient mFusedLocationClient;
    private Double lat, lng;
    private ApiInterface apiService;
    private Handler handler;
    private String summary;
    private String temperature;
    private String humidity;
    private String icon;
    private String addr;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "LocationHelper";
    private static final String KEY_LAT = "LAT";
    private static final String KEY_LNG = "LNG";

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        //this.current_activity = (Activity) context.getApplicationContext() ;
        Log.d("ssss", "co vao");
        apiService = ApiClient.getClient().create(ApiInterface.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        lat = Double.parseDouble(pref.getString(KEY_LAT, "0.000"));
        lng = Double.parseDouble(pref.getString(KEY_LNG, "0.000"));
        loadCurrentCondition();

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message == null) {
                    return false;
                }
                showNotificationMessage(context);
                return true;
            }
        });
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context) {

        Intent resultIntent = new Intent(context.getApplicationContext(), WeatherActivity.class);
        notificationUtils = new NotificationUtils(context);

        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("E HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
        String mLastUpdateTime = sdf.format(date);

        double tempD = (Double.parseDouble(temperature) - 32)*(0.5556);

        notificationUtils.showNotificationMessage(Math.round(tempD) + "°C in " + addr,
                summary,
                mLastUpdateTime,
                resultIntent);
    }

    private void loadCurrentCondition() {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat,lng, 1);
            addr = addresses.get(0).getAddressLine(0);
            if(addr == null) {
                return;
            }
            Call<ResponseBody> call = apiService.getCurentCondition(API_KEY, lat, lng);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string().toString());
                        WeatherDataCurrent weatherDataCurent = new WeatherDataCurrent();

                        summary = jsonObject.getJSONObject("currently").getString("summary");
                        Log.d(TAG, summary);

                        temperature = jsonObject.getJSONObject("currently").getString("temperature");
                        Log.d(TAG, temperature);
                        double tempD = (Double.parseDouble(temperature) - 32)*(0.5556);

                        humidity = jsonObject.getJSONObject("currently").getString("humidity");
                        Log.d(TAG, humidity);

                        icon = jsonObject.getJSONObject("currently").getString("icon");
                        Log.d(TAG, icon);
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(context.getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
