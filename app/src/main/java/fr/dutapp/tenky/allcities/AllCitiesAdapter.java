package fr.dutapp.tenky.allcities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import fr.dutapp.tenky.MainActivity;
import fr.dutapp.tenky.R;
import fr.dutapp.tenky.utils.Constants;

import static fr.dutapp.tenky.allcities.AllCitiesActivity.LATITUDE_COORDINATES;
import static fr.dutapp.tenky.allcities.AllCitiesActivity.LONGITUDE_COORDINATES;

public class AllCitiesAdapter extends RecyclerView.Adapter<AllCitiesAdapter.AllCitiesViewHolder>{

    private static final String TAG = "AllCitiesAdapter";
    private Context mContext;
    private ArrayList<String> mCityNames;
    private Map<String, Integer> iconMap;
    private SharedPreferences mPrefs;
    private RequestQueue mRequestQueue;

    public AllCitiesAdapter (Context ctx, ArrayList<String> cityNames, SharedPreferences prefs) {
        mContext = ctx;
        mCityNames = cityNames;
        mPrefs = prefs;
        this.iconMap = Constants.getIconMap();
        this.mRequestQueue = Volley.newRequestQueue(ctx);
    }
    @NonNull
    @Override
    public AllCitiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.all_cities_row, parent, false);
        return new AllCitiesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllCitiesViewHolder holder, int position) {
        String cityName = mCityNames.get(position);
        holder.mTextViewCityName.setText(cityName);
        String units = Constants.getUnits(mPrefs);

        String fullURL = Constants.BASE_URL_WEATHER + "?q=" + cityName + "&units=" + units + "&appid=" + Constants.API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject coords = resp.getJSONObject("coord");
                holder.mTextViewTempCity.setText(Math.round(resp.getJSONObject("main").getDouble("temp")) + "°");

                String iconKey = "ic_" + resp.getJSONArray("weather").getJSONObject(0).getString("icon");
                Integer iconResource = iconMap.get(iconKey);
                if (iconResource != null) {
                    holder.mImageViewWeaCity.setImageResource(iconResource);
                }

                holder.mLayout.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    try {
                        intent.putExtra(LATITUDE_COORDINATES, coords.getDouble("lat"));
                        intent.putExtra(LONGITUDE_COORDINATES, coords.getDouble("lon"));
                    } catch (JSONException e) {
                        Log.e(TAG, "Error extracting coordinates", e);
                    }
                    ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                    ((Activity) mContext).finish();
                });
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing weather data for city: " + cityName, e);
            }
        }, error -> {
            Log.e(TAG, "Error fetching weather for city: " + cityName, error);
        });

        stringRequest.setTag(TAG);
        mRequestQueue.add(stringRequest);
    }

    @Override
    public int getItemCount() {
        return mCityNames.size();
    }

    public class AllCitiesViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextViewCityName;
        private ImageView mImageViewWeaCity;
        private TextView mTextViewTempCity;
        private ConstraintLayout mLayout;

        public AllCitiesViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewCityName =itemView.findViewById(R.id.textViewHour);
            mImageViewWeaCity = itemView.findViewById(R.id.imageViewWeaHourly);
            mTextViewTempCity = itemView.findViewById(R.id.textViewTempHourly);
            mLayout = itemView.findViewById(R.id.row_Layout);
        }
    }
}
