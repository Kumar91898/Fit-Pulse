package com.kumar.fitpulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kumar.fitpulse.R;

public class splashScreen extends AppCompatActivity {
    private static final int TIMER = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(this::moveToMain, TIMER);
    }

    private void moveToMain() {
        startActivity(new Intent(this, loginScreen.class));
        finish();
    }
}