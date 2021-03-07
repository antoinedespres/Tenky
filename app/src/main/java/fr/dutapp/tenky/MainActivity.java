package fr.dutapp.tenky;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import fr.dutapp.tenky.ui.API;

import static fr.dutapp.tenky.SettingsActivity.SETTINGS_ACTIVITY_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final long MIN_TIME_BW_UPDATES = 1000;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 50;

    private TextView mTemperature;
    private TextView mCityName;
    private TextView mSunrise, mSunset, mWindSpeed;
    private TextView mJ3text;
    private TextView mJ4text;
    private LocationManager mLocationManager;
    private double mLat, mLon;
    private Location mLocation;
    private boolean mUseCelsius;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTemperature = findViewById(R.id.TemperatureValue);
        mCityName = findViewById(R.id.CityValue);
        mSunrise = findViewById(R.id.textViewSunriseValue);
        mSunset = findViewById(R.id.textViewSunsetValue);
        mWindSpeed = findViewById(R.id.textViewWindSpeedValue);
        mJ3text = findViewById(R.id.textViewJ3);
        mJ4text = findViewById(R.id.textViewJ4);

        SharedPreferences prefs = getSharedPreferences(getDefaultSharedPreferencesName(this), MODE_PRIVATE);
        this.mUseCelsius = prefs.getBoolean("temperatureUnit", true);

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
            } else {
                // First get location from Network Provider

                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        //return;
                    }
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
                    Log.d("Network", "Network");
                    if (mLocationManager != null) {
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
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                //return;
                            }
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

        weather(mLat, mLon);

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

    public void weather(double lat, double lon){
        String theUrl = API.url +lat+"&lon="+lon+"&appid="+API.apiKey;

        StringRequest str = new StringRequest(Request.Method.GET, theUrl, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject current = resp.getJSONObject("current");
                JSONArray hourly = resp.getJSONArray("hourly");
                JSONArray daily = resp.getJSONArray("daily");

                mTemperature.setText(current.getDouble("temp") +"");
                /*this.weather = this.getCurrentWeather();
                this.temp = this.getTemp();
                this.wind = this.getWind();
                this.times = this.getTimes();*/

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
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }
}