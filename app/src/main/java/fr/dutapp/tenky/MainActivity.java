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
import fr.dutapp.tenky.utils.Constants;

import static fr.dutapp.tenky.utils.Constants.ALL_CITIES_ACTIVITY_REQUEST_CODE;
import static fr.dutapp.tenky.utils.Constants.LOCATION_PERMISSION_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "MainActivity";
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
    private SharedPreferences mPrefs;
    private RecyclerView mRecyclerView;
    private RecyclerView mDailyWeatherRecyclerView;
    private DailyWeatherAdapter mDailyWeatherAdapter;
    private RequestQueue mRequestQueue;

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

        // Initialize request queue
        mRequestQueue = Volley.newRequestQueue(this);

        // Fix: Use .equals() for string comparison instead of ==
        if("fr".equals(Locale.getDefault().getLanguage())) {
            mLocale = "fr";
        } else {
            mLocale = "en";
        }

        this.mPrefs = getSharedPreferences(Constants.getDefaultSharedPreferencesName(this), MODE_PRIVATE);
        mUnits = Constants.getUnits(mPrefs);

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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                    ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
                    return;
                }
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            Constants.MIN_TIME_BW_UPDATES,
                            Constants.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                    Log.d(TAG, "Network provider enabled");
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (mLocation != null) {
                        Log.d(TAG, "Location obtained from network");
                        mLat = mLocation.getLatitude();
                        mLon = mLocation.getLongitude();
                    }
                }

                // Get the location by GPS
                if (isGPSEnabled && mLocation == null) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            Constants.MIN_TIME_BW_UPDATES,
                            Constants.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                    Log.d(TAG, "GPS provider enabled");
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (mLocation != null) {
                        Log.d(TAG, "Location obtained from GPS");
                        mLat = mLocation.getLatitude();
                        mLon = mLocation.getLongitude();
                    }
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
        } catch (Exception e) {
            Log.e(TAG, "Error getting location", e);
        }
    }

    /**
     * Reloads weather after restarting the Activity, handles settings changes
     */
    @Override
    public void onRestart() {
        super.onRestart();
        mUnits = Constants.getUnits(mPrefs);
        displayWeather(mLat, mLon);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fix: Stop location updates to prevent memory leaks
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
        // Cancel all pending requests
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
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
        Log.d(TAG, "Location changed");
        mLat = location.getLatitude();
        mLon = location.getLongitude();
        displayWeather(mLat, mLon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_OK == resultCode && data != null) {
            Log.d(TAG, "Received coordinates from AllCitiesActivity");
            // Default location: Paris, France
            mLat = data.getDoubleExtra(AllCitiesActivity.LATITUDE_COORDINATES, 48.86);
            mLon = data.getDoubleExtra(AllCitiesActivity.LONGITUDE_COORDINATES, 2.34);
            displayWeather(mLat, mLon);
        } else {
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
        String fullURL = Constants.BASE_URL_WEATHER + "?q=" + cityName + "&units=" + mUnits + "&lang=" + mLocale + "&appid=" + Constants.API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject coord = resp.getJSONObject("coord");
                double lat = coord.getDouble("lat");
                double lon = coord.getDouble("lon");
                displayWeather(lat, lon);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing weather response for city: " + cityName, e);
            }
        }, error -> {
            Log.e(TAG, "Error fetching weather for city: " + cityName, error);
        });

        stringRequest.setTag(TAG);
        mRequestQueue.add(stringRequest);
    }

    /**
     * Calls the API with coordinates and displays the weather in MainActivity
     *
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate
     */
    public void displayWeather(double lat, double lon) {
        String fullURL = Constants.BASE_URL_ONECALL + "?lat=" + lat + "&lon=" + lon + "&units=" + mUnits + "&lang=" + mLocale + "&appid=" + Constants.API_KEY;

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

                // Set background image based on weather code
                int weatherCode = w.getJSONObject(0).getInt("id");
                Map<String, Integer> imgMap = Constants.getImgMap();
                if (weatherCode == 800) {
                    Integer imgResource = imgMap.get("img_800");
                    if (imgResource != null) {
                        mBG.setImageResource(imgResource);
                    }
                } else if (weatherCode > 800) {
                    Integer imgResource = imgMap.get("img_80x");
                    if (imgResource != null) {
                        mBG.setImageResource(imgResource);
                    }
                } else {
                    int code = (weatherCode / 100) * 100;
                    Integer imgResource = imgMap.get("img_" + code);
                    if (imgResource != null) {
                        mBG.setImageResource(imgResource);
                    }
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

                // Fix: Use .equals() for string comparison
                String windSpeedUnit = Constants.UNIT_METRIC.equals(mUnits) ? " km/h" : " mph";
                mWindSpeed.setText(Math.round(current.getDouble("wind_speed")) + windSpeedUnit);

                Map<String, Integer> iconMap = Constants.getIconMap();
                Integer iconResource = iconMap.get("ic_" + w.getJSONObject(0).getString("icon"));
                if (iconResource != null) {
                    mcurrWea.setImageResource(iconResource);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing weather data", e);
            }
        }, error -> {
            Log.e(TAG, "Error fetching weather data for coordinates: " + lat + ", " + lon, error);
        });

        str.setTag(TAG);
        mRequestQueue.add(str);

        // Second API call to get the name of the city
        String cityUrl = Constants.BASE_URL_WEATHER + "?lat=" + lat + "&lon=" + lon + "&units=" + mUnits + "&lang=" + mLocale + "&appid=" + Constants.API_KEY;
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, cityUrl, response -> {
            try {
                JSONObject object = new JSONObject(response);
                mCityName.setText(object.getString("name"));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing city name", e);
            }
        }, error -> {
            Log.e(TAG, "Error fetching city name", error);
        });

        stringRequest2.setTag(TAG);
        mRequestQueue.add(stringRequest2);
    }
}