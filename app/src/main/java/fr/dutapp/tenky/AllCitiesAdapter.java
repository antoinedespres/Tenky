package fr.dutapp.tenky;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;

public class AllCitiesAdapter extends RecyclerView.Adapter<AllCitiesAdapter.AllCitiesViewHolder>{

    private Context mContext;
    private Set<String> mCityNames;

    public AllCitiesAdapter (Context ctx, Set<String> cityNames) {
        mContext = ctx;
        mCityNames = cityNames;
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
        // holder.mTextViewCityName.setText(mCityNames.get(i));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class AllCitiesViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextViewCityName;

        public AllCitiesViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewCityName =itemView.findViewById(R.id.textViewAllCitiesCityName);
        }
    }

}
