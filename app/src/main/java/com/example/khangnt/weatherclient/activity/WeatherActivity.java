package com.example.khangnt.weatherclient.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.khangnt.weatherclient.R;
import com.example.khangnt.weatherclient.adapter.CityAdapter;
import com.example.khangnt.weatherclient.adapter.WeatherDailyAdapter;
import com.example.khangnt.weatherclient.adapter.WeatherHourlyAdapter;
import com.example.khangnt.weatherclient.helper.AlarmHelper;
import com.example.khangnt.weatherclient.helper.AlarmReceiver;
import com.example.khangnt.weatherclient.helper.LocationHelper;
import com.example.khangnt.weatherclient.helper.NotificationUtils;
import com.example.khangnt.weatherclient.model.City;
import com.example.khangnt.weatherclient.model.WeatherDataCurrent;
import com.example.khangnt.weatherclient.rest.ApiClient;
import com.example.khangnt.weatherclient.rest.ApiClient1;
import com.example.khangnt.weatherclient.rest.ApiInterface;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.khangnt.weatherclient.rest.ApiClient.API_KEY;
import static com.example.khangnt.weatherclient.rest.ApiClient.API_KEY_CITY;

public class WeatherActivity extends AppCompatActivity implements
        OnItemClickListenerRecycleView, SwipeRefreshLayout.OnRefreshListener,
        LocationHelper.OnUpdateUIListener, SearchView.OnQueryTextListener {

    private final String TAG = "xxx";//WeatherActivity.class.getSimpleName();
    private Context mContext;
    private TextView txt_location, txt_status, txt_feel_like, txt_temp, txt_humidity, txt_updated, txt_powerBy;
    private RecyclerView hourly_list_view, daily_list_view, city_list_view;
    private ProgressBar loadingLayoutProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout linearLayoutimg_temp;
    private LinearLayoutManager linearLayoutManager, linearLayoutManager1, linearLayoutManager2;
    private Handler handler;
    private final int LOAD_CURRENT_CONDITION = 101;
    private final int LOAD_HOURLY_CONDITION = 103;
    private final int LOAD_DAILY_CONDITION = 105;
    private final int LOAD_CITY = 107;
    private final int LOAD_CONDITION_CITY = 109;
    private final int NO_INTERNET_CONNECTION = 111;
    private ApiInterface apiService, apiService1;
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
    private List<City> dataCity;
    private CityAdapter cityAdapter;
    private MenuItem itemSearch, itemShare;
    private boolean isLoadFromCity = false;
    private boolean isNeed2ShowNotification = false;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    // Shared preferences file name
    private static final String PREF_NAME = "Weather";
    private static final String KEY_IS_NEED_2_SHOW_NOTIFICATION = "isNeed2ShowNotification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        dataHourly = new ArrayList<>();
        dataDaily = new ArrayList<>();
        dataCity = new ArrayList<>();
        locationHelper = new LocationHelper(this);
        txt_location = (TextView)findViewById(R.id.txt_location);
        txt_status = (TextView)findViewById(R.id.txt_status);
        txt_feel_like = (TextView)findViewById(R.id.txt_feel_like);
        txt_temp = (TextView)findViewById(R.id.txt_temp);
        txt_humidity = ( TextView)findViewById(R.id.txt_humidity);
        txt_updated = (TextView)findViewById(R.id.txt_updated);
        txt_powerBy = (TextView)findViewById(R.id.txt_power_by);
        linearLayoutimg_temp = (LinearLayout)findViewById(R.id.img_temp);
        hourly_list_view = (RecyclerView)findViewById(R.id.hourly_list_view);
        hourly_list_view.setHasFixedSize(true);
        daily_list_view = (RecyclerView)findViewById(R.id.daily_list_view);
        daily_list_view.setHasFixedSize(true);
        city_list_view = (RecyclerView)findViewById(R.id.city_list_view);
        city_list_view.setHasFixedSize(true);
        city_list_view.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        loadingLayoutProgressBar = (ProgressBar)findViewById(R.id.loading_layout);
        loadingLayoutProgressBar.setVisibility(View.VISIBLE);

        linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager1 = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager2 = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);

        hourly_list_view.setLayoutManager(linearLayoutManager);
        daily_list_view.setLayoutManager(linearLayoutManager1);
        city_list_view.setLayoutManager(linearLayoutManager2);

        weatherHourlyAdapter = new WeatherHourlyAdapter(mContext, dataHourly);
        hourly_list_view.setAdapter(weatherHourlyAdapter);

        weatherDailyAdapter = new WeatherDailyAdapter(mContext, dataDaily);
        daily_list_view.setAdapter(weatherDailyAdapter);

        cityAdapter = new CityAdapter(mContext, dataCity);
        city_list_view.setAdapter(cityAdapter);

        if (locationHelper.checkPlayServices()) {
            locationHelper.init();
        }

        apiService = ApiClient.getClient().create(ApiInterface.class);
        apiService1 = ApiClient1.getClient().create(ApiInterface.class);

        txt_powerBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://darksky.net/forecast/" + latitude + "," + longitude + "/"));
                startActivity(intent);
            }
        });

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
                    case LOAD_DAILY_CONDITION:
                        weatherDailyAdapter.notifyDataSetChanged();
                        loadingLayoutProgressBar.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;
                    case LOAD_CITY:
                        cityAdapter.notifyDataSetChanged();
                        break;
                    case LOAD_CONDITION_CITY:
                        itemSearch.collapseActionView();
                        onRefreshCondition();
                        break;
                    case NO_INTERNET_CONNECTION:
                        Toast.makeText(mContext, "No internet connection", Toast.LENGTH_LONG).show();
                        finish();
                        break;
                }
                return false;
            }
        });
        pref = getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        isNeed2ShowNotification = pref.getBoolean(KEY_IS_NEED_2_SHOW_NOTIFICATION, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationHelper.stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationHelper.checkPlayServices()) {
            locationHelper.startLoadLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem itemAlarm = menu.findItem(R.id.action_alarm);

        if (!isNeed2ShowNotification) {
            itemAlarm.setIcon(R.drawable.ic_alarm_off);
        } else {
            itemAlarm.setIcon(R.drawable.ic_alarm_on);
        }

        itemSearch = menu.findItem(R.id.action_search);
        itemShare = menu.findItem(R.id.action_share_condition);
        final SearchView searchView = (SearchView) itemSearch.getActionView();
        searchView.setOnQueryTextListener(this);

        itemSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                city_list_view.setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setVisibility(View.GONE);
                itemShare.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                dataCity.clear();
                city_list_view.setVisibility(View.GONE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                itemShare.setVisible(true);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_my_location) {
            if (city_list_view.getVisibility() == View.VISIBLE) {
                itemSearch.collapseActionView();
            }
            isLoadFromCity = false;
            onRefreshCondition();
        } else if (id == R.id.action_share_condition) {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            Bitmap bm = screenShot(getWindow().getDecorView().getRootView());
                            File file = saveBitmap(bm, "mantis_image.png");
                            Log.d("Khang", "filepath: "+file.getAbsolutePath());
                            Uri uri = Uri.fromFile(new File(file.getAbsolutePath()));
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out my app.");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            shareIntent.setType("image/*");
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(shareIntent, "share via"));
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            if (response.isPermanentlyDenied()) {
                                // open device settings when the permission is
                                // denied permanently
                                locationHelper.openSettings();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        } else if (id == R.id.action_alarm) {
            if (!isNeed2ShowNotification) {
                isNeed2ShowNotification = true;
                editor.putBoolean(KEY_IS_NEED_2_SHOW_NOTIFICATION, true);
                // commit changes
                editor.commit();
                item.setIcon(R.drawable.ic_alarm_on);
                AlarmHelper.getInstance(this).setAlarm();
            } else {
                isNeed2ShowNotification = false;
                editor.putBoolean(KEY_IS_NEED_2_SHOW_NOTIFICATION, false);
                // commit changes
                editor.commit();
                item.setIcon(R.drawable.ic_alarm_off);
                AlarmHelper.getInstance(this).cancelAlarm();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        onRefreshCondition();
    }

    public void getAddress() {
        Address locationAddress;

        locationAddress = locationHelper.getAddress(latitude, longitude);

        if(locationAddress!=null) {

            address = locationAddress.getAddressLine(0);
            String address1 = locationAddress.getAddressLine(1);
            city = locationAddress.getLocality();//Dich vong hau
            state = locationAddress.getAdminArea();//Ha Noi
            String country = locationAddress.getCountryName();
            String postalCode = locationAddress.getPostalCode();
            String currentLocation;
        }
        else {
            Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
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
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, t.toString());
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHourlyCondition() {
        if (state == null || TextUtils.isEmpty(state)) {
            Toast.makeText(mContext,"No city not found", Toast.LENGTH_SHORT).show();
            return;
        }
        dataHourly.clear();
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
        dataDaily.clear();
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
                    Message message = new Message();
                    message.what = LOAD_DAILY_CONDITION;
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
                                           String feellike, String temp, String humidity, String ic) {
        txt_location.setText(city + ", " + state);
        txt_status.setText(status);
        double feellikeD = ((Double.parseDouble(feellike) - 32)*(0.5556));
        txt_feel_like.setText("Feels like " + Math.round(feellikeD) + "°C");

        double tempD = (Double.parseDouble(temp) - 32)*(0.5556);
        txt_temp.setText(Math.round(tempD) + "°C");

        double humD = (Double.parseDouble(humidity))*100;
        txt_humidity.setText("Humidity "+(int)humD+"%");
        addWeatherIc(linearLayoutimg_temp, ic);

        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("E HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
        String mLastUpdateTime = sdf.format(date);

        txt_updated.setText("updated " + mLastUpdateTime);
        txt_powerBy.setText("The Dark Sky Weather");
    }

    private void onRefreshCondition() {
        mLastLocation = locationHelper.getLocation();

        if (mLastLocation != null) {
            if (!isLoadFromCity) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }
            getAddress();
            loadCurrentCondition();
            loadHourlyCondition();
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
        parent.removeAllViews();
        parent.addView(view);
    }

    @Override
    public void onItemClick(int pos, int id) {

        if (id == R.id.root_layout_city) {
            Call<ResponseBody> call = apiService1.getDetailCity(API_KEY_CITY, dataCity.get(pos).getPlaceID());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    JSONObject jsonObject = null;
                    JSONArray jsonArray = null;

                    try {
                        jsonObject = new JSONObject(response.body().string().toString());
                        String lat = jsonObject.getJSONObject("result").getJSONObject("geometry").
                                getJSONObject("location").getString("lat");
                        String lng = jsonObject.getJSONObject("result").getJSONObject("geometry").
                                getJSONObject("location").getString("lng");
                        latitude = Double.parseDouble(lat);
                        longitude = Double.parseDouble(lng);

                        Log.d(TAG, "lat" + lat);
                        Log.d(TAG, "lng" + lng);
                        isLoadFromCity = true;
                        Message message = new Message();
                        message.what = LOAD_CONDITION_CITY;
                        handler.sendMessage(message);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("xxx", t.toString());
                    Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://darksky.net/forecast/" + latitude + "," + longitude + "/"));
            startActivity(intent);
        }
    }

    @Override
    public void updateUI() {
        onRefreshCondition();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText == null || TextUtils.isEmpty(newText)) {
            return false;
        }

        dataCity.clear();
        cityAdapter.notifyDataSetChanged();
        Log.d("Khang", "dataDaily: " + dataCity.size());

        Call<ResponseBody> call = apiService1.getListCity(API_KEY_CITY, newText);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                try {
                    jsonObject = new JSONObject(response.body().string().toString());
                    jsonArray = jsonObject.getJSONArray("predictions");
                    Log.d(TAG, jsonArray.toString());
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        City city = new City();
                        String detail = jsonArray.getJSONObject(i).getString("description");
                        String name = jsonArray.getJSONObject(i).
                                getJSONObject("structured_formatting").getString("main_text");
                        String placeid = jsonArray.getJSONObject(i).getString("place_id");
                        city.setName(name);
                        city.setDetail(detail);
                        city.setPlaceID(placeid);
                        Log.d(TAG, detail.toString());
                        dataCity.add(city);
                    }
                    Log.d("Khang", "dataCity after: " + dataCity.size());
                    Message message = new Message();
                    message.what = LOAD_CITY;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("xxx", t.toString());
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }

    public void isInternetAvailable() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final InetAddress address = InetAddress.getByName("www.google.com");
                    if (address.equals("")) {
                        Message message = new Message();
                        message.what = NO_INTERNET_CONNECTION;
                        handler.sendMessage(message);
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Message message = new Message();
                    message.what = NO_INTERNET_CONNECTION;
                    handler.sendMessage(message);
                }
                Message message = new Message();
                message.what = NO_INTERNET_CONNECTION;
                handler.sendMessage(message);
            }
        }).start();
    }

    private Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static File saveBitmap(Bitmap bm, String fileName){
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}
