package fr.dutapp.tenky;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import fr.dutapp.tenky.utils.Constants;

public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.MainActivityViewHolder>{

    private static final String TAG = "MainActivityAdapter";
    private Context mContext;
    private Map<String, Integer> iconMap;
    private SharedPreferences mPrefs;
    private Double lat;
    private Double lon;
    private RequestQueue mRequestQueue;

    public MainActivityAdapter (Context ctx, SharedPreferences prefs, double lat, double lon){
        mContext = ctx;
        mPrefs = prefs;
        this.lat = lat;
        this.lon = lon;
        this.iconMap = Constants.getIconMap();
        this.mRequestQueue = Volley.newRequestQueue(ctx);
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
        String units = Constants.getUnits(mPrefs);
        String fullURL = Constants.BASE_URL_ONECALL_25 + "?lat=" + lat + "&lon=" + lon + "&units=" + units + "&appid=" + Constants.API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONArray hourly = resp.getJSONArray("hourly");

                if (position >= hourly.length()) {
                    Log.w(TAG, "Position out of bounds: " + position);
                    return;
                }

                JSONObject hourlyData = hourly.getJSONObject(position);

                TimeZone.setDefault(TimeZone.getTimeZone(resp.getString("timezone")));
                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

                Date hour = new Date(hourlyData.getLong("dt") * 1000);

                String iconKey = "ic_" + hourlyData.getJSONArray("weather").getJSONObject(0).getString("icon");
                Integer iconResource = iconMap.get(iconKey);
                if (iconResource != null) {
                    holder.mImageViewWea.setImageResource(iconResource);
                }
                holder.mTextViewHour.setText(format.format(hour));
                holder.mTextViewTemp.setText(Math.round(hourlyData.getDouble("temp")) + "°");

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing hourly weather data at position: " + position, e);
            }
        }, error -> {
            Log.e(TAG, "Error fetching hourly weather data", error);
        });

        stringRequest.setTag(TAG);
        mRequestQueue.add(stringRequest);
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
