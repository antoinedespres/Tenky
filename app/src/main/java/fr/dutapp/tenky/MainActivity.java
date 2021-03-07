package fr.dutapp.tenky;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final long MIN_TIME_BW_UPDATES = 1000;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 50;
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
    private TextView mJ8MMtext;
    private TextView mJ9MMtext;
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




    @SuppressLint("WrongViewCast")
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





        SharedPreferences prefs = getSharedPreferences(getDefaultSharedPreferencesName(this), MODE_PRIVATE);
        this.mUseCelsius = prefs.getBoolean("temperatureUnit", true);


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
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
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

        String theUrl = url +mLat+"&lon="+mLon+"&appid="+apiKey;

        StringRequest str = new StringRequest(Request.Method.GET, theUrl, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject current = resp.getJSONObject("current");
                JSONArray hourly = resp.getJSONArray("hourly");
                JSONArray daily = resp.getJSONArray("daily");
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                SimpleDateFormat fora = new SimpleDateFormat("E");


                mTemperature.setText(String.format("%.2g",current.getDouble("temp") - 272.15)+"°");
                mHumidity.setText(current.getDouble("humidity")+"");
                mFeelsLike.setText(String.format("%.2g",current.getDouble("feels_like") - 272.15)+"°" );
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


                mTomorrowtext.setText(String.format("%.2g", tomorrow.getJSONObject("temp").getDouble("day")- 272.15)+"°");
                mTomorrowMMtext.setText(String.format("%.2g", tomorrow.getJSONObject("temp").getDouble("max")- 272.15)+"°/"+String.format("%.2g", tomorrow.getJSONObject("temp").getDouble("min")- 272.15)+"°");

                mJ2text.setText(String.format("%.2g", day2.getJSONObject("temp").getDouble("day")- 272.15)+"°");
                mJ2MMtext.setText(String.format("%.2g", day2.getJSONObject("temp").getDouble("max")- 272.15)+"°/"+String.format("%.2g", day2.getJSONObject("temp").getDouble("min")- 272.15)+"°");

                mJ3text.setText(String.format("%.2g", day3.getJSONObject("temp").getDouble("day")- 272.15)+"°");
                mJ3MMtext.setText(String.format("%.2g", day3.getJSONObject("temp").getDouble("max")- 272.15)+"°/"+String.format("%.2g", day3.getJSONObject("temp").getDouble("min")- 272.15)+"°");

                mJ4text.setText(String.format("%.2g", day4.getJSONObject("temp").getDouble("day")- 272.15)+"°");
                mJ4MMtext.setText(String.format("%.2g", day4.getJSONObject("temp").getDouble("max")- 272.15)+"°/"+String.format("%.2g", day4.getJSONObject("temp").getDouble("min")- 272.15)+"°");

                mJ5text.setText(String.format("%.2g", day5.getJSONObject("temp").getDouble("day")- 272.15)+"°");
                mJ5MMtext.setText(String.format("%.2g", day5.getJSONObject("temp").getDouble("max")- 272.15)+"°/"+String.format("%.2g", day5.getJSONObject("temp").getDouble("min")- 272.15)+"°");

                mJ6text.setText(String.format("%.2g", day6.getJSONObject("temp").getDouble("day")- 272.15)+"°");
                mJ6MMtext.setText(String.format("%.2g", day6.getJSONObject("temp").getDouble("max")- 272.15)+"°/"+String.format("%.2g", day6.getJSONObject("temp").getDouble("min")- 272.15)+"°");

                mJ7text.setText(String.format("%.2g", day7.getJSONObject("temp").getDouble("day")- 272.15)+"°");
                mJ7MMtext.setText(String.format("%.2g", day7.getJSONObject("temp").getDouble("max")- 272.15)+"°/"+String.format("%.2g", day7.getJSONObject("temp").getDouble("min")- 272.15)+"°");

                mWindSpeed.setText(String.format("%.2g", current.getDouble("wind_speed")));

                Picasso.get().load("http://openweathermap.org/img/wn/"+w.getJSONObject(0).getString("icon")+"@4x.png").into(mcurrWea);

                Picasso.get().load("http://openweathermap.org/img/wn/"+tomorrow.getJSONArray("weather").getJSONObject(0).getString("icon")+"@4x.png").into(mWeaTom);
                Picasso.get().load("http://openweathermap.org/img/wn/"+day2.getJSONArray("weather").getJSONObject(0).getString("icon")+"@4x.png").into(mWeaJ2);
                Picasso.get().load("http://openweathermap.org/img/wn/"+day3.getJSONArray("weather").getJSONObject(0).getString("icon")+"@4x.png").into(mWeaJ3);
                Picasso.get().load("http://openweathermap.org/img/wn/"+day4.getJSONArray("weather").getJSONObject(0).getString("icon")+"@4x.png").into(mWeaJ4);
                Picasso.get().load("http://openweathermap.org/img/wn/"+day5.getJSONArray("weather").getJSONObject(0).getString("icon")+"@4x.png").into(mWeaJ5);
                Picasso.get().load("http://openweathermap.org/img/wn/"+day6.getJSONArray("weather").getJSONObject(0).getString("icon")+"@4x.png").into(mWeaJ6);
                Picasso.get().load("http://openweathermap.org/img/wn/"+day7.getJSONArray("weather").getJSONObject(0).getString("icon")+"@4x.png").into(mWeaJ7);

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

        String Url = "https://api.openweathermap.org/data/2.5/weather?lat="+mLat+"&lon="+mLon+"&appid="+apiKey;
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

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(getDefaultSharedPreferencesName(this), MODE_PRIVATE);
        this.mUseCelsius = prefs.getBoolean("temperatureUnit", true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.navigation_all_cities:
                startActivity(new Intent(this, AllCitiesActivity.class));
                break;
            case R.id.navigation_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == SETTINGS_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
                String city = data.getStringExtra(CITY_NAME);
                API api  = new API(city);
                if (city == null) {
                    api.getWeather("Paris");
                } else {
                    api.getWeather(city);
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }catch(Exception e){

        }
    }*/

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

/*    public void weather(double lat, double lon){
        try {
            API api = new API(lat, lon, MainActivity.this);

            mCityName.setText(API.getCityName(lat, lon));
            mSunrise.setText(api.getTimes().get(1));
            mSunset.setText(api.getTimes().get(2));
            mWindSpeed.setText(api.getWind().get(0).toString());

        }catch(Exception e){
            e.printStackTrace();
        }
    }*/


    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }
}