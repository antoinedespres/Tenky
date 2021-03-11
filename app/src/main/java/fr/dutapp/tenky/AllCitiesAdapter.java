package fr.dutapp.tenky;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AllCitiesAdapter extends RecyclerView.Adapter<AllCitiesAdapter.AllCitiesViewHolder>{

    private Context mContext;
    private ArrayList<String> mCityNames;
    private Map iconMap;
    private SharedPreferences mPrefs;

    public AllCitiesAdapter (Context ctx, ArrayList<String> cityNames, SharedPreferences prefs) {
        mContext = ctx;
        mCityNames = cityNames;
        mPrefs = prefs;

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
    public AllCitiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.all_cities_row, parent, false);
        return new AllCitiesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllCitiesViewHolder holder, int position) {
        // TODO
        String cityName = mCityNames.get(position);
        holder.mTextViewCityName.setText(cityName);


        String fullURL = "https://api.openweathermap.org/data/2.5/weather?q="+cityName+"&appid="+ MainActivity.apiKey;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, response -> {
            try {
                JSONObject resp = new JSONObject(response);
                JSONObject coords = resp.getJSONObject("coord");
                if(mPrefs.getBoolean("temperatureUnit", true))
                    holder.mTextViewTempCity.setText(String.format("%.2g",resp.getJSONObject("main").getDouble("temp")-273.15));
                else
                    holder.mTextViewTempCity.setText(String.format("%.2g",(resp.getJSONObject("main").getDouble("temp")-273.15)* 9 / 5 + 32));

                holder.mImageViewWeaCity.setImageResource((int) this.iconMap.get("ic_" + resp.getJSONArray("weather").getJSONObject(0).getString("icon")));
                holder.mLayout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Intent intent = new Intent(mContext, MainActivity.class);
                        try {
                            intent.putExtra("lat",coords.getInt("lat"));
                            intent.putExtra("lon", coords.getInt("lon"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                        ((Activity) mContext).finish();
                    }
                });
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

        // holder.mTextViewCityName.setText(mCityNames.get(i));
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
            mTextViewCityName =itemView.findViewById(R.id.textViewAllCitiesCityName);
            mImageViewWeaCity = itemView.findViewById(R.id.imageViewWeaAllCities);
            mTextViewTempCity = itemView.findViewById(R.id.textViewTempAllCities);
            mLayout = itemView.findViewById(R.id.row_Layout);
        }
    }

}
