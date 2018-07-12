package com.example.khangnt.weatherclient.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.khangnt.weatherclient.R;
import com.example.khangnt.weatherclient.adapter.WeatherDailyAdapter;
import com.example.khangnt.weatherclient.adapter.WeatherHourlyAdapter;
import com.example.khangnt.weatherclient.locationutil.LocationHelper;
import com.example.khangnt.weatherclient.model.WeatherDataCurrent;
import com.example.khangnt.weatherclient.rest.ApiClient;
import com.example.khangnt.weatherclient.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.thbs.skycons.library.CloudFogView;
import com.thbs.skycons.library.CloudMoonView;
import com.thbs.skycons.library.CloudRainView;
import com.thbs.skycons.library.CloudSnowView;
import com.thbs.skycons.library.CloudSunView;
import com.thbs.skycons.library.CloudView;
import com.thbs.skycons.library.MoonView;
import com.thbs.skycons.library.SunView;
import com.thbs.skycons.library.WindView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.khangnt.weatherclient.rest.ApiClient.API_KEY;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback {

    private final String TAG = "xxx";//WeatherActivity.class.getSimpleName();
    private Context mContext;
    private TextView txt_location, txt_status, txt_feel_like, txt_temp, txt_humidity;
    private RecyclerView hourly_list_view, daily_list_view;
    private LinearLayout linearLayoutimg_temp;
    private LinearLayoutManager linearLayoutManager, linearLayoutManager1;
    private Handler handler;
    private final int LOAD_CURRENT_CONDITION = 101;
    private final int LOAD_HOURLY_CONDITION = 103;
    private final int LOAD_DAYLY_CONDITION = 105;
    private ApiInterface apiService;
    private LocationHelper locationHelper;
    private Location mLastLocation;
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String state;
    private List<WeatherDataCurrent> dataHourly;
    private WeatherHourlyAdapter weatherHourlyAdapter;
    private List<WeatherDataCurrent> dataDaily;
    private WeatherDailyAdapter weatherDailyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        dataHourly = new ArrayList<>();
        dataDaily = new ArrayList<>();
        locationHelper=new LocationHelper(this);
        locationHelper.checkpermission();
        txt_location = (TextView)findViewById(R.id.txt_location);
        txt_status = (TextView)findViewById(R.id.txt_status);
        txt_feel_like = (TextView)findViewById(R.id.txt_feel_like);
        txt_temp = (TextView)findViewById(R.id.txt_temp);
        txt_humidity = ( TextView)findViewById(R.id.txt_humidity);
        linearLayoutimg_temp = (LinearLayout)findViewById(R.id.img_temp);
        hourly_list_view = (RecyclerView)findViewById(R.id.hourly_list_view);
        daily_list_view = (RecyclerView)findViewById(R.id.daily_list_view);

        linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager1 = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);

        hourly_list_view.setLayoutManager(linearLayoutManager);
        daily_list_view.setLayoutManager(linearLayoutManager1);

        weatherHourlyAdapter = new WeatherHourlyAdapter(mContext, dataHourly);
        hourly_list_view.setAdapter(weatherHourlyAdapter);

        weatherDailyAdapter = new WeatherDailyAdapter(mContext, dataDaily);
        daily_list_view.setAdapter(weatherDailyAdapter);

        if (locationHelper.checkPlayServices()) {
            // Building the GoogleApi client
            locationHelper.buildGoogleApiClient();
        }

        apiService =
                ApiClient.getClient().create(ApiInterface.class);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message == null) {
                    return false;
                }

                switch (message.what) {
                    case LOAD_CURRENT_CONDITION:
                        break;
                    case LOAD_HOURLY_CONDITION:
                        weatherHourlyAdapter.notifyDataSetChanged();
                        break;
                    case LOAD_DAYLY_CONDITION:
                        weatherDailyAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode,resultCode,data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        locationHelper.checkPlayServices();
        //onRefreshCondition();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // redirects to utils
        locationHelper.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }

    public void getAddress() {
        Address locationAddress;

        locationAddress=locationHelper.getAddress(latitude, longitude);

        if(locationAddress!=null) {

            address = locationAddress.getAddressLine(0);
            String address1 = locationAddress.getAddressLine(1);
            city = locationAddress.getLocality();
            state = locationAddress.getAdminArea();
            String country = locationAddress.getCountryName();
            String postalCode = locationAddress.getPostalCode();
            String currentLocation;
        }
        else {
            Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation=locationHelper.getLocation();
        onRefreshCondition();
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationHelper.connectApiClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("Connection failed:", " ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void loadCurrentCondition() {
        if (state == null || TextUtils.isEmpty(state)) {
            Toast.makeText(mContext,"No city not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ResponseBody> call = apiService.getCurentCondition(API_KEY, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string().toString());
                    WeatherDataCurrent weatherDataCurent = new WeatherDataCurrent();

                    weatherDataCurent.setWeatherText(
                            jsonObject.getJSONObject("currently").getString("summary"));
                    Log.d(TAG, jsonObject.getJSONObject("currently").getString("summary"));

                    weatherDataCurent.setWindSpeed(
                            jsonObject.getJSONObject("currently").getString("windSpeed"));
                    Log.d(TAG, jsonObject.getJSONObject("currently").getString("windSpeed"));

                    weatherDataCurent.setTemperature(
                            jsonObject.getJSONObject("currently").getString("temperature"));
                    Log.d(TAG, jsonObject.getJSONObject("currently").getString("temperature"));

                    weatherDataCurent.setRealFeelTemperature(
                            jsonObject.getJSONObject("currently").getString("apparentTemperature"));
                    Log.d(TAG, jsonObject.getJSONObject("currently").getString("apparentTemperature"));

                    weatherDataCurent.setRelativeHumidity(
                            jsonObject.getJSONObject("currently").getString("humidity"));
                    Log.d(TAG, jsonObject.getJSONObject("currently").getString("humidity"));

                    weatherDataCurent.setWeatherIcon(
                            jsonObject.getJSONObject("currently").getString("icon"));
                    Log.d(TAG, jsonObject.getJSONObject("currently").getString("icon"));

                    weatherDataCurent.setMobileLink(
                            "https://darksky.net/forecast/" + latitude + "," + longitude + "/");
                    Log.d(TAG, "https://darksky.net/forecast/" + latitude + "," + longitude + "/");

                    setCurrentConditionToView(weatherDataCurent.getWeatherText(),
                            weatherDataCurent.getRealFeelTemperature(),
                            weatherDataCurent.getTemperature(),
                            weatherDataCurent.getRelativeHumidity(),
                            weatherDataCurent.getWeatherIcon());
                    Log.d(TAG, jsonObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //JsonObject jsonObject = new JsonObject()
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("xxx", t.toString());
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHoulyCondition() {
        if (state == null || TextUtils.isEmpty(state)) {
            Toast.makeText(mContext,"No city not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ResponseBody> call = apiService.getHourlyCondition(API_KEY, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                try {
                    jsonObject = new JSONObject(response.body().string().toString());
                    jsonArray = jsonObject.getJSONObject("hourly").getJSONArray("data");
                    Log.d(TAG, jsonArray.toString());
                    for (int i = 0; i < jsonArray.length(); ++ i) {
                        WeatherDataCurrent weatherDataCurent = new WeatherDataCurrent();

                        weatherDataCurent.setWeatherText(
                                jsonArray.getJSONObject(i).getString("summary"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("summary"));

                        weatherDataCurent.setWindSpeed(
                                jsonArray.getJSONObject(i).getString("windSpeed"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("windSpeed"));

                        weatherDataCurent.setTemperature(
                                jsonArray.getJSONObject(i).getString("temperature"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("temperature"));

                        weatherDataCurent.setRealFeelTemperature(
                                jsonArray.getJSONObject(i).getString("apparentTemperature"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("apparentTemperature"));

                        weatherDataCurent.setRelativeHumidity(
                                jsonArray.getJSONObject(i).getString("humidity"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("humidity"));

                        weatherDataCurent.setWeatherIcon(
                                jsonArray.getJSONObject(i).getString("icon"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("icon"));

                        weatherDataCurent.setTime(
                                jsonArray.getJSONObject(i).getString("time"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("time"));

                        weatherDataCurent.setMobileLink(
                                "https://darksky.net/forecast/" + latitude + "," + longitude + "/");
                        Log.d(TAG, "https://darksky.net/forecast/" + latitude + "," + longitude + "/");

                        dataHourly.add(weatherDataCurent);
                    }
                    //weatherHourlyAdapter.notifyDataSetChanged();
                    Message message = new Message();
                    message.what = LOAD_HOURLY_CONDITION;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //JsonObject jsonObject = new JsonObject()
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("xxx", t.toString());
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDailyCondition() {
        if (state == null || TextUtils.isEmpty(state)) {
            Toast.makeText(mContext,"No city not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ResponseBody> call = apiService.getDailyCondition(API_KEY, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                try {
                    jsonObject = new JSONObject(response.body().string().toString());
                    jsonArray = jsonObject.getJSONObject("daily").getJSONArray("data");
                    Log.d(TAG, jsonArray.toString());
                    for (int i = 0; i < jsonArray.length(); ++ i) {
                        WeatherDataCurrent weatherDataCurent = new WeatherDataCurrent();

                        weatherDataCurent.setWeatherText(
                                jsonArray.getJSONObject(i).getString("summary"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("summary"));

                        weatherDataCurent.setWindSpeed(
                                jsonArray.getJSONObject(i).getString("windSpeed"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("windSpeed"));

                        weatherDataCurent.setTemperatureMax(
                                jsonArray.getJSONObject(i).getString("temperatureHigh"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("temperatureHigh"));

                        weatherDataCurent.setTemperatureMin(
                                jsonArray.getJSONObject(i).getString("temperatureLow"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("temperatureLow"));

                        weatherDataCurent.setRelativeHumidity(
                                jsonArray.getJSONObject(i).getString("humidity"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("humidity"));

                        weatherDataCurent.setWeatherIcon(
                                jsonArray.getJSONObject(i).getString("icon"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("icon"));

                        weatherDataCurent.setTime(
                                jsonArray.getJSONObject(i).getString("time"));
                        Log.d(TAG, jsonArray.getJSONObject(i).getString("time"));

                        weatherDataCurent.setMobileLink(
                                "https://darksky.net/forecast/" + latitude + "," + longitude + "/");
                        Log.d(TAG, "https://darksky.net/forecast/" + latitude + "," + longitude + "/");

                        dataDaily.add(weatherDataCurent);
                    }
                    //weatherHourlyAdapter.notifyDataSetChanged();
                    Message message = new Message();
                    message.what = LOAD_DAYLY_CONDITION;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //JsonObject jsonObject = new JsonObject()
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("xxx", t.toString());
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCurrentConditionToView(String status,
                                           String feellike, String temp, String hummidity, String ic) {
        txt_location.setText(city);
        txt_status.setText(status);
        double feellikeD = ((Double.parseDouble(feellike) - 32)*(0.5556));
        txt_feel_like.setText("Feels like " + Math.round(feellikeD));

        double tempD = (Double.parseDouble(temp) - 32)*(0.5556);
        txt_temp.setText(Math.round(tempD)+"");

        double humD = (Double.parseDouble(hummidity))*100;
        txt_humidity.setText("Humidity "+(int)humD+"%");
        addWeatherIc(linearLayoutimg_temp, ic);
    }

    private void onRefreshCondition() {
        mLastLocation = locationHelper.getLocation();

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            getAddress();
            loadCurrentCondition();
            loadHoulyCondition();
            loadDailyCondition();
        }
    }

    private void addWeatherIc(LinearLayout parent, String ic)  {
        View view = null;
        LinearLayout.LayoutParams params;

        if (ic.equals("clear-day")) {
            view = new SunView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary), 
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("clear-night")) {
            view = new MoonView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("rain")) {
            view = new CloudRainView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("snow")) {
            view = new CloudSnowView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("sleet")) {
            view = new CloudSnowView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("wind")) {
            view = new WindView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("fog")) {
            view = new CloudFogView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("cloudy")) {
            view = new CloudView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("partly-cloudy-day")) {
            view = new CloudSunView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("partly-cloudy-night")) {
            view = new CloudMoonView(this,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        }

        if (view != null) {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.width = 260;
            params.height = 260;
            view.setLayoutParams(params);
        }
        parent.addView(view);
    }
}
