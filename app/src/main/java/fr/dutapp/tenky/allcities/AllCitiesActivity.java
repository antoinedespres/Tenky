package fr.dutapp.tenky.allcities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import fr.dutapp.tenky.R;
import fr.dutapp.tenky.settings.SettingsActivity;
import fr.dutapp.tenky.utils.Constants;

public class AllCitiesActivity extends AppCompatActivity {

    private static final String TAG = "AllCitiesActivity";
    public static final String LATITUDE_COORDINATES = "LATITUDE_COORDINATES";
    public static final String LONGITUDE_COORDINATES = "LONGITUDE_COORDINATES";
    public static final String CITY_LIST = "CITY_LIST";

    private SharedPreferences mPrefs;
    ArrayList<String> mCityNames;
    private Button mAdd;
    private Button mClear;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_cities);

        mRecyclerView = findViewById(R.id.recycler_view_all_cities);
        mPrefs = getSharedPreferences(Constants.getDefaultSharedPreferencesName(this), MODE_PRIVATE);
        mAdd = findViewById(R.id.buttonAdd);
        mClear = findViewById(R.id.button_reset_list);

        mCityNames = new ArrayList<>();
        for (int i = 0; i < mPrefs.getInt("nbrCities", 0); ++i) {
            String cityName = mPrefs.getString("ville" + i, "");
            if (!cityName.isEmpty()) {
                mCityNames.add(cityName);
            }
        }

        mAdd.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(AllCitiesActivity.this);
            dialog.setContentView(R.layout.custom_dialog);
            dialog.setTitle("Title");

            Button button = dialog.findViewById(R.id.dialog_ok);
            Button button_cancel = dialog.findViewById(R.id.dialog_cancel);

            button.setOnClickListener(v12 -> {
                EditText edit = dialog.findViewById(R.id.cityname_text);
                String cityName = edit.getText().toString().trim();

                if (!cityName.isEmpty()) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    int currentCount = mPrefs.getInt("nbrCities", 0);
                    editor.putString("ville" + currentCount, cityName);
                    editor.putInt("nbrCities", currentCount + 1);
                    editor.apply();

                    dialog.dismiss();
                    // Reload the Activity
                    finish();
                    startActivity(getIntent());
                } else {
                    Log.w(TAG, "City name is empty");
                }
            });

            button_cancel.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

        mClear.setOnClickListener(v -> {
            SharedPreferences.Editor edit = mPrefs.edit();
            int cityCount = mPrefs.getInt("nbrCities", 0);
            for (int i = 0; i < cityCount; ++i) {
                edit.remove("ville" + i);
            }
            edit.putInt("nbrCities", 0);
            edit.apply();

            finish();
            startActivity(getIntent());
        });

        AllCitiesAdapter mAllCitiesAdapter = new AllCitiesAdapter(this, mCityNames, mPrefs);
        mRecyclerView.setAdapter(mAllCitiesAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.navigation_all_cities);
        item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.navigation_all_cities:
                startActivity(new Intent(this, AllCitiesActivity.class));
                finish();
                break;
            case R.id.navigation_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}