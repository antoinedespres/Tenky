package fr.dutapp.tenky;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.dutapp.tenky.utils.Constants;

public class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherAdapter.DailyWeatherViewHolder> {

    private Context mContext;
    private List<DailyWeather> mDailyWeatherList;
    private Map<String, Integer> iconMap;

    public DailyWeatherAdapter(Context context, List<DailyWeather> dailyWeatherList) {
        mContext = context;
        mDailyWeatherList = new ArrayList<>(dailyWeatherList);
        this.iconMap = Constants.getIconMap();
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
        mDailyWeatherList.clear();
        mDailyWeatherList.addAll(newDailyWeatherList);
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
