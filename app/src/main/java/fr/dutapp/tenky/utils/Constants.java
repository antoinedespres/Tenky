package fr.dutapp.tenky.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import fr.dutapp.tenky.BuildConfig;
import fr.dutapp.tenky.R;

public class Constants {

    // API Configuration - Move this to BuildConfig or local.properties in production
    public static final String API_KEY = "2d16a26b9de301ad0c8d42865664d50e";
    public static final String BASE_URL_ONECALL = "https://api.openweathermap.org/data/3.0/onecall";
    public static final String BASE_URL_WEATHER = "https://api.openweathermap.org/data/2.5/weather";
    public static final String BASE_URL_ONECALL_25 = "https://api.openweathermap.org/data/2.5/onecall";

    // Location updates
    public static final long MIN_TIME_BW_UPDATES = 300000; // 5 minutes
    public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5000; // 5 km

    // Request codes
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int ALL_CITIES_ACTIVITY_REQUEST_CODE = 2;

    // Units
    public static final String UNIT_METRIC = "metric";
    public static final String UNIT_IMPERIAL = "imperial";

    // Splash screen delay
    public static final int SPLASH_SCREEN_DELAY = 800;

    // Weather icon mapping
    private static Map<String, Integer> iconMap;
    private static Map<String, Integer> imgMap;

    public static Map<String, Integer> getIconMap() {
        if (iconMap == null) {
            iconMap = new HashMap<>();
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
        }
        return iconMap;
    }

    public static Map<String, Integer> getImgMap() {
        if (imgMap == null) {
            imgMap = new HashMap<>();
            imgMap.put("img_200", R.drawable.img_200);
            imgMap.put("img_300", R.drawable.img_300);
            imgMap.put("img_500", R.drawable.img_500);
            imgMap.put("img_600", R.drawable.img_600);
            imgMap.put("img_700", R.drawable.img_700);
            imgMap.put("img_800", R.drawable.img_800);
            imgMap.put("img_80x", R.drawable.img_80x);
        }
        return imgMap;
    }

    public static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    public static String getUnits(SharedPreferences prefs) {
        return prefs.getBoolean("unitChoice", false) ? UNIT_IMPERIAL : UNIT_METRIC;
    }
}
