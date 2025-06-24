package com.example.mednotifyplus.Reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;

import com.example.mednotifyplus.R;

import java.util.ArrayList;

public class MainActivityReminder extends AppCompatActivity {
    DBHelperReminder dbHelperReminder;
    RecyclerView recyclerView;
    ReminderAdapter adapter;
    ArrayList<Reminder> reminderList;
    Button btnAddReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_reminder);

        createNotificationChannel();

        dbHelperReminder = new DBHelperReminder(this);
        recyclerView = findViewById(R.id.recyclerView);
        btnAddReminder = findViewById(R.id.btnAddReminder);

        reminderList = new ArrayList<>();
        loadReminders();

        adapter = new ReminderAdapter(this, reminderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAddReminder.setOnClickListener(v -> {
            startActivity(new Intent(MainActivityReminder.this, AddReminderActivity.class));
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm_sound);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    "mednotify_channel",
                    "Medicine Reminders",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription("Channel for medicine reminder alarms");
            channel.setSound(alarmSound, audioAttributes);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void loadReminders() {
        reminderList.clear();
        Cursor cursor = dbHelperReminder.getAllReminders();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String dosage = cursor.getString(2);
            long time = cursor.getLong(3);
            reminderList.add(new Reminder(id, name, dosage, time));
        }
        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
        adapter.notifyDataSetChanged();
    }
}