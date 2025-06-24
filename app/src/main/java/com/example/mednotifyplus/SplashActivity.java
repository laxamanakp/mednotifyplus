package com.example.mednotifyplus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // See step 2

        // Delay for 2 seconds then launch MainMenuActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainMenuActivity.class));
            finish();
        }, 2000);
    }
}
