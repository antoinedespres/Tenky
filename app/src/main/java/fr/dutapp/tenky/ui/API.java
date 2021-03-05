package fr.dutapp.tenky.ui;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.reflect.Array;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.*;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

import fr.dutapp.tenky.MainActivity;


public class API {
    public final static String apiKey = "6b9eb0c4a410dfaf06f6fa358eb6ffba";
    public final static String url = "https://api.openweathermap.org/data/2.5/onecall?lat=";

    private JSONObject resp;
    private JSONObject current;
    private JSONArray daily;
    private JSONArray hourly;
    private String cityName;
    private StringRequest str;

    private ArrayList<String> weather;
    private HashMap<String, Double> temp;
    private ArrayList<Double> wind;
    private ArrayList<String> times;

    public API(double lat, double lon, Context ctx) throws Exception{
        String theUrl = url+lat+"&lon="+lon+"&appid="+apiKey;


        this.str = new StringRequest(Request.Method.GET, theUrl, response -> {
            try {
                this.resp = new JSONObject(response);
                this.current = this.resp.getJSONObject("current");
                this.hourly = this.resp.getJSONArray("hourly");
                this.daily = this.resp.getJSONArray("daily");

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
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(this.str);
    }

    public API(String city, Context ctx)throws Exception{
        this(API.getCoord(city).get(0), API.getCoord(city).get(1), ctx);
    }

    public StringRequest getStr(){
        return this.str;
    }

    public static ArrayList<Double> getCoord(String city) {
        String Url = "https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+apiKey;
        ArrayList<Double> ret = new ArrayList<>();
        StringRequest str = new StringRequest(Request.Method.GET, Url, response -> {

            try {
                JSONObject object = new JSONObject(response);
                JSONObject coords = object.getJSONObject("coords");
                ret.add(coords.getDouble("lat"));
                ret.add(coords.getDouble("lon"));
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

        return ret;
    }

    public static String getCityName(double lat, double lon) {
        String Url = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid="+apiKey;
        AtomicReference<String> name = new AtomicReference<>();
        StringRequest str = new StringRequest(Request.Method.GET, Url, response -> {
            try {

                JSONObject object = new JSONObject(response);
                name.set(object.getString("name"));
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

        return name.get();

    }

    public ArrayList<String> getCurrentWeather() throws JSONException {
        ArrayList<String> ret = new ArrayList<>();

        JSONObject wea = this.current.getJSONObject("weather");

        ret.add(wea.getString("main"));
        ret.add(wea.getString("description"));
        ret.add(wea.getString("icon"));

        return ret;
    }

    public HashMap<String, Double> getTemp() throws JSONException {
        HashMap<String, Double> ret = new HashMap<>();

        ret.put("temp",  this.current.getDouble("temp"));
        ret.put("feels_like",  this.current.getDouble("feels_like"));
        ret.put("temp_min", this.current.getDouble("temp_min"));
        ret.put("temp_max",  this.current.getDouble("temp_max"));
        ret.put("humidity",  this.current.getDouble("humidity"));

        return ret;
    }

    public ArrayList<Double> getWind() throws JSONException {
        ArrayList<Double> ret = new ArrayList<>();
        ret.add(this.current.getDouble("wind_speed"));
        ret.add(this.current.getDouble("wind_deg"));
        return ret;

    }

    public ArrayList<String> getTimes() throws JSONException {
        ArrayList<String> ret = new ArrayList<>();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        int timezone = this.resp.getInt("timezone_offset");

        Date sunrise = new Date((this.current.getLong("sunrise") + timezone) * 1000);
        Date sunset = new Date((this.current.getLong("sunset") + timezone) * 1000);

        ret.add(format.format(sunrise));
        ret.add(format.format(sunset));
        return ret;
    }



    @SuppressWarnings("finally")
//    public static ArrayList<Object> getByCoords(double lat, double lon) {
//        ArrayList<Object> all = new ArrayList<>();
//        try {
//            URL app = new URL("https://" + url + "?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey);
//            try {
//                JSONObject api = (JSONObject) new JSONTokener(InputToString(app.openStream())).nextValue();
//                String cityName = api.getString("name");
//
//                all.add(cityName);
//                all.add(API.getWeather(cityName));
//                all.add(API.getTemp(cityName));
//                all.add(API.getTimes(cityName));
//                all.add(API.getWind(cityName));
//                all.add(API.getIcon(cityName));
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return all;
//    }


    static String InputToString(InputStream i)
            throws IOException {
        String originalString = "";

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (i, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

}
