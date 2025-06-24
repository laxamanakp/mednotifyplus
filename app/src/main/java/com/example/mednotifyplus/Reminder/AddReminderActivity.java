package com.example.mednotifyplus.Reminder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.ActivityNotFoundException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mednotifyplus.R;

import java.util.Calendar;

public class AddReminderActivity extends AppCompatActivity {
    EditText etName, etDosage;
    TimePicker timePicker;
    Button btnSave, btnChangeSound;
    DBHelperReminder dbHelperReminder;

    private static final int REQUEST_SOUND_PICK = 1;
    private static final int REQUEST_READ_AUDIO = 1001;
    private static final int REQUEST_POST_NOTIFICATION = 1002;

    private Uri selectedSoundUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        etName = findViewById(R.id.etName);
        etDosage = findViewById(R.id.etDosage);
        timePicker = findViewById(R.id.timePicker);
        btnSave = findViewById(R.id.btnSave);
        btnChangeSound = findViewById(R.id.btnChangeSound);
        dbHelperReminder = new DBHelperReminder(this);

        requestNecessaryPermissions();
        checkExactAlarmPermission(); // ✅ Keep this for Android 12+

        btnChangeSound.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            try {
                startActivityForResult(intent, REQUEST_SOUND_PICK);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No app found to pick audio", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();

            if (name.isEmpty() || dosage.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            long timeInMillis = calendar.getTimeInMillis();

            long id = dbHelperReminder.addReminder(name, dosage, timeInMillis,
                    selectedSoundUri != null ? selectedSoundUri.toString() : null);

            scheduleAlarm((int) id, name, dosage, timeInMillis);
            Toast.makeText(this, "Reminder Set", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void scheduleAlarm(int requestCode, String name, String dosage, long timeInMillis) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("dosage", dosage);
        if (selectedSoundUri != null) {
            intent.putExtra("soundUri", selectedSoundUri.toString());
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    private void requestNecessaryPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        REQUEST_READ_AUDIO);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATION);
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SOUND_PICK && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                selectedSoundUri = data.getData();
                try {
                    getContentResolver().takePersistableUriPermission(
                            selectedSoundUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    Toast.makeText(this, "Alarm sound selected!", Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    Toast.makeText(this, "Permission error accessing sound", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
