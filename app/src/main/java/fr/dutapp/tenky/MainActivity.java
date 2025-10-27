package fr.dutapp.tenky;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import fr.dutapp.tenky.allcities.AllCitiesActivity;
import fr.dutapp.tenky.settings.SettingsActivity;

import static fr.dutapp.tenky.allcities.AllCitiesActivity.ALL_CITIES_ACTIVITY_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static final int MAIN_ACTIVITY_REQUEST_CODE = 1;

    private static final long MIN_TIME_BW_UPDATES = 300000;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5000;
    public final static String apiKey = "d70a31d1df42f92474ac82c282c143e8";
    public final static String url = "https://api.openweathermap.org/data/2.5/onecall?lat=";
    private String mLocale;

    private TextView mTemperature;
    private TextView mCityName;
    private TextView mSunrise, mSunset, mWindSpeed;
    private TextView mFeelsLike;
    private TextView mHumidity;
    private TextView mDesc;
    private LocationManager mLocationManager;
    private double mLat, mLon;
    private Location mLocation;
    private String mUnits;
    private ImageView mcurrWea;
    private ImageView mBG;
    private Map<String, Integer> iconMap;
    private Map<String, Integer> imgMap;
    private SharedPreferences mPrefs;
    private RecyclerView mRecyclerView;
    private RecyclerView mDailyWeatherRecyclerView;
    private DailyWeatherAdapter mDailyWeatherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBG = findViewById(R.id.imageViewBG);
        mTemperature = findViewById(R.id.TemperatureValue);
        mCityName = findViewById(R.id.CityValue);
        mSunrise = findViewById(R.id.textViewSunriseValue);
        mSunset = findViewById(R.id.textViewSunsetValue);
        mWindSpeed = findViewById(R.id.textViewWindSpeedValue);
        mFeelsLike = findViewById(R.id.textViewFeelsLike);
        mHumidity = findViewById(R.id.textViewHumidity);
        mDesc = findViewById(R.id.textViewDesc);
        mcurrWea = findViewById(R.id.imageCurrentWea);
        mRecyclerView = findViewById(R.id.main_activity_recycler_view);
        mDailyWeatherRecyclerView = findViewById(R.id.daily_weather_recycler_view);

        this.iconMap = new HashMap<>();
        iconMap.put("ic_01d", R.drawable.ic_01d);
        iconMap.put("ic_01n", R.drawable.ic_01n);
        iconMap.put("ic_02d", R.drawable.ic_02d);
        iconMap.put("ic_02n", R.drawable.ic_02n);
        iconMap.put("ic_03d", R.drawable.ic_03d);
        iconMap.put("ic_03n", R.drawable.ic_03n);
        iconMap.put("ic_04d", R.drawable.ic_04d);
        iconMap.put("ic_04n", R.drawable.ic_04n);
        iconMap.put("ic_09d", R.drawable.ic_09d);
        iconMap.put("ic_09n", R.drawable.ic_09n);
        iconMap.put("ic_10d", R.drawable.ic_10d);
        iconMap.put("ic_10n", R.drawable.ic_10n);
        iconMap.put("ic_11d", R.drawable.ic_11d);
        iconMap.put("ic_11n", R.drawable.ic_11n);
        iconMap.put("ic_13d", R.drawable.ic_13d);
        iconMap.put("ic_13n", R.drawable.ic_13n);
        iconMap.put("ic_50d", R.drawable.ic_50d);
        iconMap.put("ic_50n", R.drawable.ic_50n);

        this.imgMap = new HashMap<>();
        imgMap.put("img_200", R.drawable.img_200);
        imgMap.put("img_300", R.drawable.img_300);
        imgMap.put("img_500", R.drawable.img_500);
        imgMap.put("img_600", R.drawable.img_600);
        imgMap.put("img_700", R.drawable.img_700);
        imgMap.put("img_800", R.drawable.img_800);
        imgMap.put("img_80x", R.drawable.img_80x);

        if(Locale.getDefault().getLanguage() == "fr") {
            mLocale = "fr";
        } else {
            mLocale = "en";
        }
        this.mPrefs = getSharedPreferences(getDefaultSharedPreferencesName(this), MODE_PRIVATE);
        mUnits = mPrefs.getBoolean("unitChoice", false) ? "imperial" : "metric";

        getCoordinates();
        displayWeather(mLat, mLon);

        MainActivityAdapter mActivity = new MainActivityAdapter(this, mPrefs, mLat, mLon);
        LinearLayoutManager l = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setAdapter(mActivity);
        mRecyclerView.setLayoutManager(l);

        mDailyWeatherAdapter = new DailyWeatherAdapter(this, new ArrayList<>());
        LinearLayoutManager dailyLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mDailyWeatherRecyclerView.setAdapter(mDailyWeatherAdapter);
        mDailyWeatherRecyclerView.setLayoutManager(dailyLayoutManager);
    }

    public void getCoordinates() {
        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
            } else {
                // First get location from Network Provider
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                    ActivityCompat.requestPermissions(this, permissions, 1);
                }
                if (isNetworkEnabled) {

                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network enabled");
                    if (mLocationManager != null) {
                        mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLocation != null) {
                            Log.d("location", "by network");
                            mLat = mLocation.getLatitude();
                            mLon = mLocation.getLongitude();
                        }
                    }
                }

                //get the location by gps
                if (isGPSEnabled) {
                    if (mLocation == null) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mLocationManager != null) {

                            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mLocation != null) {
                                Log.d("location", "by gps");
                                mLat = mLocation.getLatitude();
                                mLon = mLocation.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads weather after restarting the Activity, handles settings changes
     */
    @Override
    public void onRestart() {
        super.onRestart();
        mUnits = mPrefs.getBoolean("unitChoice", false) ? "imperial" : "metric";
        displayWeather(mLat, mLon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.navigation_all_cities:
                Intent intent = new Intent(this, AllCitiesActivity.class);
                startActivityForResult(intent, ALL_CITIES_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.navigation_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("locationchanged", "moved!");
        //getCoordinates();
        //displayWeather(mLat, mLon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_OK == resultCode) {
            Log.d("onActivityResult", "Method called in MainActivity");
            // Default location: Paris, France
            mLat = data.getDoubleExtra(AllCitiesActivity.LATITUDE_COORDINATES, 48.86);
            mLon = data.getDoubleExtra(AllCitiesActivity.LONGITUDE_COORDINATES, 2.34);
            displayWeather(mLat, mLon);
        }
        else{
            getCoordinates();
            displayWeather(mLat, mLon);
        }

    }

    /**
     * Display weather data from a city name
     *
     * @param cityName The name of the city
     */
    public void displayWeather(String cityName) {
        String fullURL = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=" + mUnits + "&lang=" + mLocale + "&appid=" + apiKey;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject coord = resp.getJSONObject("coord");
                double lat = coord.getDouble("lat");
                double lon = coord.getDouble("lon");
                displayWeather(lat, lon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            try {
                throw new Exception("Bad URL request");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    /**
     * Calls the API with coordinates and displays the weather in MainActivity
     *
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate
     */
    public void displayWeather(double lat, double lon) {
        String fullURL = url + lat + "&lon=" + lon + "&units=" + mUnits + "&lang=" + mLocale + "&appid=" + apiKey;

        StringRequest str = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject current = resp.getJSONObject("current");
                JSONArray hourly = resp.getJSONArray("hourly");
                JSONArray daily = resp.getJSONArray("daily");
                TimeZone.setDefault(TimeZone.getTimeZone(resp.getString("timezone")));
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                SimpleDateFormat fora = new SimpleDateFormat("E");

                mTemperature.setText(Math.round(current.getDouble("temp")) + "°");
                mHumidity.setText(current.getDouble("humidity") + " %");
                mFeelsLike.setText(Math.round(current.getDouble("feels_like")) + "°");
                JSONArray w = current.getJSONArray("weather");
                mDesc.setText(w.getJSONObject(0).getString("description"));

                if(w.getJSONObject(0).getInt("id") == 800){
                    mBG.setImageResource((int) this.imgMap.get("img_800"));
                }else if(w.getJSONObject(0).getInt("id") > 800){
                    mBG.setImageResource((int) this.imgMap.get("img_80x"));
                }

                else {
                    int code = (int) (w.getJSONObject(0).getInt("id")/100) * 100;

                    Log.d("image", code + "");
                    mBG.setImageResource((int) this.imgMap.get("img_" + code));

                }
                mBG.setScaleType(ImageView.ScaleType.CENTER_CROP);


                Date sunrise = new Date((current.getLong("sunrise")) * 1000);
                Date sunset = new Date((current.getLong("sunset")) * 1000);

                mSunrise.setText(format.format(sunrise));
                mSunset.setText(format.format(sunset));

                List<DailyWeather> dailyWeatherList = new ArrayList<>();

                for (int i = 1; i <= 7; i++) {
                    JSONObject dayData = daily.getJSONObject(i);
                    Date dayDate = new Date(dayData.getLong("dt") * 1000);
                    String dayName = fora.format(dayDate);
                    int temperature = (int) Math.round(dayData.getJSONObject("temp").getDouble("day"));
                    String minMaxTemp = Math.round(dayData.getJSONObject("temp").getDouble("max")) + "°/" +
                                       Math.round(dayData.getJSONObject("temp").getDouble("min")) + "°";
                    String weatherIcon = "ic_" + dayData.getJSONArray("weather").getJSONObject(0).getString("icon");

                    dailyWeatherList.add(new DailyWeather(dayName, temperature, minMaxTemp, weatherIcon));
                }

                mDailyWeatherAdapter.updateData(dailyWeatherList);

                String windSpeedUnit = mUnits == "metric" ? " km/h" : " mph";
                mWindSpeed.setText(Math.round(current.getDouble("wind_speed")) + windSpeedUnit);

                mcurrWea.setImageResource((int) this.iconMap.get("ic_" + w.getJSONObject(0).getString("icon")));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            try {
                throw new Exception("Bad URL request");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(str);

        // second API call to get the name of the city
        String Url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=" + mUnits + "&lang=" + mLocale + "&appid=" + apiKey;
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, Url, response -> {
            try {

                JSONObject object = new JSONObject(response);
                mCityName.setText(object.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            try {
                throw new Exception("Bad URL request");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        requestQueue.add(stringRequest2);



    }

    /**
     * Gets the default SharedPreferences' file name
     *
     * @param context
     * @return Default SharedPreferences' file name
     */
    public static final String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }
}