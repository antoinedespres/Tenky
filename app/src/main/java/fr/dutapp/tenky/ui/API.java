package fr.dutapp.tenky.ui;

import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.*;

import java.io.*;


public interface API {
    final static String apiKey = "6b9eb0c4a410dfaf06f6fa358eb6ffba";
    final static String url = "api.openweathermap.org/data/2.5/weather";

    public static ArrayList<Float> getCoord(String city) {
        ArrayList<Float> ret = new ArrayList<>();
        try {
            URL app = new URL("https://" + url + "?q=" + city + "&appid=" + apiKey);
            try {
                JSONObject api = (JSONObject) new JSONTokener(InputToString(app.openStream())).nextValue();
                JSONObject coord = api.getJSONObject("coord");
                ret.add((float) coord.getDouble("lat"));
                ret.add((float) coord.getDouble("lon"));
                return ret;

            } catch (IOException e) {
                System.out.println("Cette ville n'existe pas");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static ArrayList<String> getWeather(String city) {
        ArrayList<String> ret = new ArrayList<>();
        try {
            URL app = new URL("https://" + url + "?q=" + city + "&appid=" + apiKey);
            try {
                JSONObject api = (JSONObject) new JSONTokener(InputToString(app.openStream())).nextValue();
                JSONArray w = api.getJSONArray("weather");
                JSONObject weather = w.getJSONObject(0);

                ret.add(weather.getString("main"));
                ret.add(weather.getString("description"));
                return ret;

            } catch (IOException e) {
                System.out.println("Cette ville n'existe pas");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static HashMap<String, Float> getTemp(String city) {
        HashMap<String, Float> ret = new HashMap<>();
        try {
            URL app = new URL("https://" + url + "?q=" + city + "&appid=" + apiKey);
            try {
                JSONObject api = (JSONObject) new JSONTokener(InputToString(app.openStream())).nextValue();
                JSONObject coord = api.getJSONObject("main");
                ret.put("temp", (float) coord.getDouble("temp"));
                ret.put("feels_like", (float) coord.getDouble("feels_like"));
                ret.put("temp_min", (float) coord.getDouble("temp_min"));
                ret.put("temp_max", (float) coord.getDouble("temp_max"));
                ret.put("humidity", (float) coord.getDouble("humidity"));
                return ret;

            } catch (IOException e) {
                System.out.println("Cette ville n'existe pas");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<Float> getWind(String city) {
        ArrayList<Float> ret = new ArrayList<>();
        try {
            URL app = new URL("https://" + url + "?q=" + city + "&appid=" + apiKey);
            try {
                JSONObject api = (JSONObject) new JSONTokener(InputToString(app.openStream())).nextValue();
                JSONObject coord = api.getJSONObject("wind");
                ret.add((float) coord.getDouble("speed"));
                ret.add((float) coord.getDouble("deg"));
                return ret;

            } catch (IOException e) {
                System.out.println("Cette ville n'existe pas");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<String> getTimes(String city) {
        ArrayList<String> ret = new ArrayList<>();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        try {
            URL app = new URL("https://" + url + "?q=" + city + "&appid=" + apiKey);
            try {
                JSONObject api = (JSONObject) new JSONTokener(InputToString(app.openStream())).nextValue();
                JSONObject coord = api.getJSONObject("sys");

                int timezone = api.getInt("timezone");

                Date sunrise = new Date((coord.getLong("sunrise") + timezone) * 1000);
                Date sunset = new Date((coord.getLong("sunset") + timezone) * 1000);

                ret.add(format.format(sunrise));
                ret.add(format.format(sunset));


                return ret;

            } catch (IOException e) {
                System.out.println("Cette ville n'existe pas");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        return null;
    }

    public static String getIcon(String city) {

        try {
            URL app = new URL("https://" + url + "?q=" + city + "&appid=" + apiKey);
            try {
                JSONObject api = (JSONObject) new JSONTokener(InputToString(app.openStream())).nextValue();
                JSONArray w = api.getJSONArray("weather");
                JSONObject weather = w.getJSONObject(0);

                return weather.getString("icon");


            } catch (IOException e) {
                System.out.println("Cette ville n'existe pas");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("finally")
    public static ArrayList<Object> getByCoords(double lat, double lon) {
        ArrayList<Object> all = new ArrayList<>();
        try {
            URL app = new URL("https://" + url + "?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey);
            try {
                JSONObject api = (JSONObject) new JSONTokener(InputToString(app.openStream())).nextValue();
                String cityName = api.getString("name");

                all.add(cityName);
                all.add(API.getWeather(cityName));
                all.add(API.getTemp(cityName));
                all.add(API.getTimes(cityName));
                all.add(API.getWind(cityName));
                all.add(API.getIcon(cityName));


            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return all;
    }


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
