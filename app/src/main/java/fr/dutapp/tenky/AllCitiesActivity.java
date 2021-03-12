package fr.dutapp.tenky;

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
import android.widget.ImageButton;

import java.util.ArrayList;

import static fr.dutapp.tenky.MainActivity.getDefaultSharedPreferencesName;

public class AllCitiesActivity extends AppCompatActivity {

    public static final int ALL_CITIES_ACTIVITY_REQUEST_CODE = 2;
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
        mPrefs = getSharedPreferences(getDefaultSharedPreferencesName(this), MODE_PRIVATE);
        mAdd = findViewById(R.id.buttonAdd);
        mClear = findViewById(R.id.button_reset_list);

        mCityNames = new ArrayList<>();
        for (int i = 0; i < mPrefs.getInt("nbrCities", 0); ++i) {
            mCityNames.add(mPrefs.getString("ville" + i + "", ""));
        }

        mAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog dialog = new Dialog(AllCitiesActivity.this);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setTitle("Title");

                Button button = dialog.findViewById(R.id.dialog_ok);
                Button button_cancel = dialog.findViewById(R.id.dialog_cancel);

                button.setOnClickListener(v12 -> {

                    EditText edit = dialog.findViewById(R.id.cityname_text);
                    String cityName = edit.getText().toString();

                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("ville" + mPrefs.getInt("nbrCities", 0) + "", cityName);
                    editor.putInt("nbrCities", mPrefs.getInt("nbrCities", 0) + 1);
                    editor.apply();

                    dialog.dismiss();
                    // Reload the Activity
                    finish();
                    startActivity(getIntent());
                });

                button_cancel.setOnClickListener(v1 -> dialog.dismiss());

                dialog.show();
            }
        });

        mClear.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SharedPreferences.Editor edit = mPrefs.edit();
                for (int i = 0; i < mPrefs.getInt("nbrCities", 0); ++i) {
                    edit.remove("ville" + i + "");
                    edit.apply();
                }

                edit.putInt("nbrCities", 0);
                edit.apply();
                finish();
                startActivity(getIntent());
            }
        });

        Log.d("oui", mPrefs.getAll().toString());

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

    public int getSize() {
        return mCityNames.size();
    }
}