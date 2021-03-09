package fr.dutapp.tenky;

import android.Manifest;
import android.annotation.SuppressLint;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static fr.dutapp.tenky.AllCitiesActivity.ALL_CITIES_ACTIVITY_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static final int  MAIN_ACTIVITY_REQUEST_CODE = 1;

    private static final long MIN_TIME_BW_UPDATES = 300000;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5000;
    public final static String apiKey = "6b9eb0c4a410dfaf06f6fa358eb6ffba";
    public final static String url = "https://api.openweathermap.org/data/2.5/onecall?lat=";

    private TextView mTemperature;
    private TextView mCityName;
    private TextView mSunrise, mSunset, mWindSpeed;
    private TextView mTomorrowtext;
    private TextView mTomorrowMMtext;
    private TextView mJ2;
    private TextView mJ3;
    private TextView mJ4;
    private TextView mJ5;
    private TextView mJ6;
    private TextView mJ7;
    private TextView mJ2text;
    private TextView mJ3text;
    private TextView mJ4text;
    private TextView mJ5text;
    private TextView mJ6text;
    private TextView mJ7text;
    private TextView mJ8text;
    private TextView mJ9text;
    private TextView mJ2MMtext;
    private TextView mJ3MMtext;
    private TextView mJ4MMtext;
    private TextView mJ5MMtext;
    private TextView mJ6MMtext;
    private TextView mJ7MMtext;

    private TextView mFeelsLike;
    private TextView mMinTemp;
    private TextView mMaxTemp;
    private TextView mHumidity;
    private TextView mDesc;
    private LocationManager mLocationManager;
    private double mLat, mLon;
    private Location mLocation;
    private boolean mUseCelsius;
    private ImageView mcurrWea;
    private ImageView mWeaTom;
    private ImageView mWeaJ2;
    private ImageView mWeaJ3;
    private ImageView mWeaJ4;
    private ImageView mWeaJ5;
    private ImageView mWeaJ6;
    private ImageView mWeaJ7;
    private Map iconMap;
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTemperature = findViewById(R.id.TemperatureValue);
        mCityName = findViewById(R.id.CityValue);
        mSunrise = findViewById(R.id.textViewSunriseValue);
        mSunset = findViewById(R.id.textViewSunsetValue);
        mWindSpeed = findViewById(R.id.textViewWindSpeedValue);
        mFeelsLike = findViewById(R.id.textViewFeelsLike);
        mMinTemp = findViewById(R.id.textViewMinMaxJ2);
        mMaxTemp = findViewById(R.id.textViewMinMaxTom);
        mHumidity = findViewById(R.id.textViewHumidity);
        mDesc = findViewById(R.id.textViewDesc);
        mTomorrowtext = findViewById(R.id.textViewJ1value);
        mJ2text = findViewById(R.id.textViewJ2value);
        mJ3text = findViewById(R.id.textViewJ3value);
        mJ4text = findViewById(R.id.textViewJ4value);
        mJ5text = findViewById(R.id.textViewJ5value);
        mJ6text = findViewById(R.id.textViewJ6value);
        mJ7text = findViewById(R.id.textViewJ7value);
        mJ2MMtext = findViewById(R.id.textViewMinMaxJ2);
        mJ3MMtext = findViewById(R.id.textViewMinMaxJ3);
        mJ4MMtext = findViewById(R.id.textViewMinMaxJ4);
        mJ5MMtext = findViewById(R.id.textViewMinMaxJ5);
        mJ6MMtext = findViewById(R.id.textViewMinMaxJ6);
        mJ7MMtext = findViewById(R.id.textViewMinMaxJ7);
        mTomorrowMMtext = findViewById(R.id.textViewMinMaxTom);
        mcurrWea = findViewById(R.id.imageCurrentWea);
        mWeaJ2 = findViewById(R.id.imageViewJ2);
        mWeaJ3 = findViewById(R.id.imageViewJ3);
        mWeaJ4 = findViewById(R.id.imageViewJ4);
        mWeaJ5 = findViewById(R.id.imageViewJ5);
        mWeaJ6 = findViewById(R.id.imageViewJ6);
        mWeaJ7 = findViewById(R.id.imageViewJ7);
        mWeaTom = findViewById(R.id.imageViewTom);
        mJ2 = findViewById(R.id.textViewJ2);
        mJ3 = findViewById(R.id.textViewJ3);
        mJ4 = findViewById(R.id.textViewJ4);
        mJ5 = findViewById(R.id.textViewJ5);
        mJ6 = findViewById(R.id.textViewJ6);
        mJ7 = findViewById(R.id.textViewJ7);

        this.iconMap = new HashMap<String, Drawable>();
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

        this.prefs = getSharedPreferences(getDefaultSharedPreferencesName(this), MODE_PRIVATE);
        this.mUseCelsius = prefs.getBoolean("temperatureUnit", true);


        getCoordinates();
        displayWeather(mLat, mLon);
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
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return;
                }
                if (isNetworkEnabled) {

                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
                    Log.d("Network", "Network");
                    if (mLocationManager != null) {
                        mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLocation != null) {
                            mLat = mLocation.getLatitude();
                            mLon = mLocation.getLongitude();
                        }
                    }
                }
                //get the location by gps
                if (isGPSEnabled) {
                    if (mLocation == null) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mLocationManager != null) {

                            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mLocation != null) {
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
        mUseCelsius = prefs.getBoolean("temperatureUnit", true);
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
        Log.d("locationchanged", "moved!!");
        //getCoordinates();
        //displayWeather(mLat, mLon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (ALL_CITIES_ACTIVITY_REQUEST_CODE == requestCode && RESULT_OK == resultCode) {
            Log.d("activityforresult", "%%%%%%%%%%%%");
            // Default location: Paris, France
            mLat = data.getDoubleExtra(AllCitiesActivity.LATITUDE_COORDINATES, 48.86);
            mLon = data.getDoubleExtra(AllCitiesActivity.LONGITUDE_COORDINATES, 2.34);
            displayWeather(mLat, mLon);
        }

    }

    /**
     * Display weather data from a city name
     * @param cityName The name of the city
     */
    public void displayWeather(String cityName) {
        String fullURL = "https://api.openweathermap.org/data/2.5/weather?q="+cityName+"&appid="+apiKey;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject coord = resp.getJSONObject("coord");
                double lat = coord.getDouble("lat");
                double lon = coord.getDouble("lon");
                displayWeather(lat, lon);
            } catch (JSONException e){
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
        String fullURL = url + lat + "&lon=" + lon + "&appid=" + apiKey;

        StringRequest str = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject current = resp.getJSONObject("current");
                JSONArray hourly = resp.getJSONArray("hourly");
                JSONArray daily = resp.getJSONArray("daily");
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                SimpleDateFormat fora = new SimpleDateFormat("E");

                mTemperature.setText(String.format("%.2g", convertTemp(current.getDouble("temp"))) + "°");
                mHumidity.setText(current.getDouble("humidity") + " %");
                mFeelsLike.setText(String.format("%.2g", convertTemp(current.getDouble("feels_like"))) + "°");
                JSONArray w = current.getJSONArray("weather");
                mDesc.setText(w.getJSONObject(0).getString("description"));

                int timezone = resp.getInt("timezone_offset");

                Date sunrise = new Date((current.getLong("sunrise") + timezone) * 1000);
                Date sunset = new Date((current.getLong("sunset") + timezone) * 1000);

                mSunrise.setText(format.format(sunrise));
                mSunset.setText(format.format(sunset));

                JSONObject tomorrow = daily.getJSONObject(1);
                JSONObject day2 = daily.getJSONObject(2);
                JSONObject day3 = daily.getJSONObject(3);
                JSONObject day4 = daily.getJSONObject(4);
                JSONObject day5 = daily.getJSONObject(5);
                JSONObject day6 = daily.getJSONObject(6);
                JSONObject day7 = daily.getJSONObject(7);

                Log.d("jsp", day2.toString());
                Log.d("jsp", day3.toString());


                mTomorrowtext.setText(String.format("%.2g", convertTemp(tomorrow.getJSONObject("temp").getDouble("day"))) + "°");
                mTomorrowMMtext.setText(String.format("%.2g", convertTemp(tomorrow.getJSONObject("temp").getDouble("max"))) + "°/" + String.format("%.2g", convertTemp(tomorrow.getJSONObject("temp").getDouble("min"))) + "°");

                mJ2text.setText(String.format("%.2g", convertTemp(day2.getJSONObject("temp").getDouble("day"))) + "°");
                mJ2MMtext.setText(String.format("%.2g", convertTemp(day2.getJSONObject("temp").getDouble("max"))) + "°/" + String.format("%.2g", convertTemp(day2.getJSONObject("temp").getDouble("min"))) + "°");

                mJ3text.setText(String.format("%.2g", convertTemp(day3.getJSONObject("temp").getDouble("day"))) + "°");
                mJ3MMtext.setText(String.format("%.2g", convertTemp(day3.getJSONObject("temp").getDouble("max"))) + "°/" + String.format("%.2g", convertTemp(day3.getJSONObject("temp").getDouble("min"))) + "°");

                mJ4text.setText(String.format("%.2g", convertTemp(day4.getJSONObject("temp").getDouble("day"))) + "°");
                mJ4MMtext.setText(String.format("%.2g", convertTemp(day4.getJSONObject("temp").getDouble("max"))) + "°/" + String.format("%.2g", convertTemp(day4.getJSONObject("temp").getDouble("min"))) + "°");

                mJ5text.setText(String.format("%.2g", convertTemp(day5.getJSONObject("temp").getDouble("day"))) + "°");
                mJ5MMtext.setText(String.format("%.2g", convertTemp(day5.getJSONObject("temp").getDouble("max"))) + "°/" + String.format("%.2g", convertTemp(day5.getJSONObject("temp").getDouble("min"))) + "°");

                mJ6text.setText(String.format("%.2g", convertTemp(day6.getJSONObject("temp").getDouble("day"))) + "°");
                mJ6MMtext.setText(String.format("%.2g", convertTemp(day6.getJSONObject("temp").getDouble("max"))) + "°/" + String.format("%.2g", convertTemp(day6.getJSONObject("temp").getDouble("min"))) + "°");

                mJ7text.setText(String.format("%.2g", convertTemp(day7.getJSONObject("temp").getDouble("day"))) + "°");
                mJ7MMtext.setText(String.format("%.2g", convertTemp(day7.getJSONObject("temp").getDouble("max"))) + "°/" + String.format("%.2g", convertTemp(day7.getJSONObject("temp").getDouble("min"))) + "°");

                // wind speed in kph
                mWindSpeed.setText(String.format("%.2g", current.getDouble("wind_speed") * 3.6 ) + " km/h");

                mcurrWea.setImageResource((int) this.iconMap.get("ic_" + w.getJSONObject(0).getString("icon")));

                mWeaTom.setImageResource((int) this.iconMap.get("ic_" + tomorrow.getJSONArray("weather").getJSONObject(0).getString("icon")));
                mWeaJ2.setImageResource((int) this.iconMap.get("ic_" + day2.getJSONArray("weather").getJSONObject(0).getString("icon")));
                mWeaJ3.setImageResource((int) this.iconMap.get("ic_" + day3.getJSONArray("weather").getJSONObject(0).getString("icon")));
                mWeaJ4.setImageResource((int) this.iconMap.get("ic_" + day4.getJSONArray("weather").getJSONObject(0).getString("icon")));
                mWeaJ5.setImageResource((int) this.iconMap.get("ic_" + day5.getJSONArray("weather").getJSONObject(0).getString("icon")));
                mWeaJ6.setImageResource((int) this.iconMap.get("ic_" + day6.getJSONArray("weather").getJSONObject(0).getString("icon")));
                mWeaJ7.setImageResource((int) this.iconMap.get("ic_" + day7.getJSONArray("weather").getJSONObject(0).getString("icon")));

                mJ2.setText(fora.format(new Date((day2.getLong("dt") + timezone) * 1000)));
                mJ3.setText(fora.format(new Date((day3.getLong("dt") + timezone) * 1000)));
                mJ4.setText(fora.format(new Date((day4.getLong("dt") + timezone) * 1000)));
                mJ5.setText(fora.format(new Date((day5.getLong("dt") + timezone) * 1000)));
                mJ6.setText(fora.format(new Date((day6.getLong("dt") + timezone) * 1000)));
                mJ7.setText(fora.format(new Date((day7.getLong("dt") + timezone) * 1000)));

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
        String Url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;
        StringRequest str2 = new StringRequest(Request.Method.GET, Url, response -> {
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

        requestQueue.add(str2);
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

    public double convertTemp(double temp) {
        if (this.mUseCelsius)
            return temp - 273.15;
        return (temp - 273.15) * 9 / 5 + 32;
    }
}