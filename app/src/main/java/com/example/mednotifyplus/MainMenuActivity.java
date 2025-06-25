package com.example.mednotifyplus;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mednotifyplus.Admin.AdminLoginActivity;

import com.example.mednotifyplus.Cost.MainActivityCost;
import com.example.mednotifyplus.Reminder.MainActivityReminder;
import com.example.mednotifyplus.databinding.ActivityMainMenuBinding;

public class MainMenuActivity extends AppCompatActivity {

    private ActivityMainMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Required for splash screen (Android 12+)
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Button listeners
        binding.btnReminder.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivityReminder.class)));

        binding.btnCostSuggest.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivityCost.class)));

        binding.btnAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLoginActivity.class)));
    }
}
