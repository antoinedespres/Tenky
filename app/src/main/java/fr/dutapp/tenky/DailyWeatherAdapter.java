package fr.dutapp.tenky;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherAdapter.DailyWeatherViewHolder> {

    private Context mContext;
    private List<DailyWeather> mDailyWeatherList;
    private Map<String, Integer> iconMap;

    public DailyWeatherAdapter(Context context, List<DailyWeather> dailyWeatherList) {
        mContext = context;
        mDailyWeatherList = dailyWeatherList;

        this.iconMap = new HashMap<>();
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
    public DailyWeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.daily_weather_item, parent, false);
        return new DailyWeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyWeatherViewHolder holder, int position) {
        DailyWeather dailyWeather = mDailyWeatherList.get(position);

        holder.mDayName.setText(dailyWeather.getDayName());
        holder.mTemperature.setText(dailyWeather.getTemperature() + "°");
        holder.mMinMaxTemp.setText(dailyWeather.getMinMaxTemp());

        Integer iconResource = iconMap.get(dailyWeather.getWeatherIcon());
        if (iconResource != null) {
            holder.mWeatherIcon.setImageResource(iconResource);
        }
    }

    @Override
    public int getItemCount() {
        return mDailyWeatherList.size();
    }

    public void updateData(List<DailyWeather> newDailyWeatherList) {
        mDailyWeatherList = newDailyWeatherList;
        notifyDataSetChanged();
    }

    public class DailyWeatherViewHolder extends RecyclerView.ViewHolder {
        private TextView mDayName;
        private TextView mTemperature;
        private TextView mMinMaxTemp;
        private ImageView mWeatherIcon;

        public DailyWeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            mDayName = itemView.findViewById(R.id.textViewDayName);
            mTemperature = itemView.findViewById(R.id.textViewDayTemp);
            mMinMaxTemp = itemView.findViewById(R.id.textViewMinMax);
            mWeatherIcon = itemView.findViewById(R.id.imageViewDayWeather);
        }
    }
}
