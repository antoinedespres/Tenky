package fr.dutapp.tenky;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));

        // prevents the user from using the return button to go back to the splash screen
        finish();
    }
}