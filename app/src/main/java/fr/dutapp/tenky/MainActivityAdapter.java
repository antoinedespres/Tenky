package fr.dutapp.tenky;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.MainActivityViewHolder>{

    private Context mContext;
    private Map iconMap;
    private SharedPreferences mPrefs;
    private Double lat;
    private Double lon;



    public MainActivityAdapter (Context ctx, SharedPreferences prefs, double lat, double lon){
        mContext = ctx;
        mPrefs = prefs;
        this.lat = lat;
        this.lon = lon;
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

    }

    @NonNull
    @Override
    public MainActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.main_activity_row, parent, false);

        return new MainActivityViewHolder((view));
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivityViewHolder holder, int position) {

        String units = mPrefs.getBoolean("unitChoice", false) ? "imperial" : "metric";

        String fullURL = "https://api.openweathermap.org/data/2.5/onecall?lat="+lat+ "&lon=" + lon +"&units=" +  units + "&appid="+ MainActivity.apiKey;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONArray hourly = resp.getJSONArray("hourly");
                JSONObject houly = hourly.getJSONObject(position);

                TimeZone.setDefault(TimeZone.getTimeZone(resp.getString("timezone")));
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                //int timezone = resp.getInt("timezone_offset");

                Date hour = new Date((houly.getLong("dt")) * 1000);

                holder.mImageViewWea.setImageResource((int) this.iconMap.get("ic_" + houly.getJSONArray("weather").getJSONObject(0).getString("icon") ));
                holder.mTextViewHour.setText(format.format(hour));
                holder.mTextViewTemp.setText(Math.round(houly.getDouble("temp")) + "°");


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
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);


    }


    @Override
    public int getItemCount() {
        return 24;
    }

    public class MainActivityViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextViewHour;
        private ImageView mImageViewWea;
        private TextView mTextViewTemp;


        public MainActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewHour =itemView.findViewById(R.id.textViewHour);
            mImageViewWea = itemView.findViewById(R.id.imageViewWeaHourly);
            mTextViewTemp = itemView.findViewById(R.id.textViewTempHourly);

        }
    }
}
