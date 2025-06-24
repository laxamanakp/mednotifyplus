package com.example.mednotifyplus.Reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mednotifyplus.R;

import java.util.Calendar;

public class EditReminderActivity extends AppCompatActivity {
    EditText etName, etDosage;
    TimePicker timePicker;
    Button btnUpdate, btnDelete;
    DBHelperReminder dbHelperReminder;
    int reminderId;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminder);

        etName = findViewById(R.id.etName);
        etDosage = findViewById(R.id.etDosage);
        timePicker = findViewById(R.id.timePicker);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        dbHelperReminder = new DBHelperReminder(this);

        Intent intent = getIntent();
        reminderId = intent.getIntExtra("id", -1);
        etName.setText(intent.getStringExtra("name"));
        etDosage.setText(intent.getStringExtra("dosage"));

        long time = intent.getLongExtra("time", 0);
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));

        btnUpdate.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String dosage = etDosage.getText().toString();

            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // ✅ Fix: If selected time is in the past, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            long newTime = calendar.getTimeInMillis();

            dbHelperReminder.updateReminder(reminderId, name, dosage, newTime);
            scheduleAlarm(reminderId, name, dosage, newTime);
            Toast.makeText(this, "Reminder Updated", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            cancelAlarm(reminderId);
            dbHelperReminder.deleteReminder(reminderId);
            Toast.makeText(this, "Reminder Deleted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void scheduleAlarm(int requestCode, String name, String dosage, long timeInMillis) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("dosage", dosage);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    private void cancelAlarm(int requestCode) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
