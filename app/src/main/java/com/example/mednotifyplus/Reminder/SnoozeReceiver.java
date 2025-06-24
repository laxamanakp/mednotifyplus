package com.example.mednotifyplus.Reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        String dosage = intent.getStringExtra("dosage");

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("name", name);
        alarmIntent.putExtra("dosage", dosage);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);

        NotificationManagerCompat.from(context).cancel(1001);
    }
}
