package com.example.mednotifyplus.Reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mednotifyplus.R;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String DEFAULT_CHANNEL_ID = "mednotify_channel_default";
    private static final String CHANNEL_ID_PREFIX = "mednotify_channel_";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("name");
        String dosage = intent.getStringExtra("dosage");
        String soundUriString = intent.getStringExtra("soundUri");

        Uri alarmSound = (soundUriString != null && !soundUriString.isEmpty())
                ? Uri.parse(soundUriString)
                : Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm_sound);

        // Create or get notification channel for this sound URI
        String channelId = getOrCreateNotificationChannel(context, alarmSound);

        // STOP action
        Intent stopIntent = new Intent(context, StopReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // SNOOZE action (5 minutes)
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        snoozeIntent.putExtra("name", medicineName);
        snoozeIntent.putExtra("dosage", dosage);
        snoozeIntent.putExtra("soundUri", soundUriString);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Medicine Reminder")
                .setContentText(medicineName + " - " + dosage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Do NOT call setSound here, channel controls sound on Android 8+
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Snooze 5 mins", snoozePendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, builder.build());
    }

    private String getOrCreateNotificationChannel(Context context, Uri soundUri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // No channel needed on pre-Oreo
            return "default";
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId;

        if (soundUri == null) {
            channelId = DEFAULT_CHANNEL_ID;
            if (notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Medicine Reminder",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setSound(
                        android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build());
                notificationManager.createNotificationChannel(channel);
            }
        } else {
            channelId = CHANNEL_ID_PREFIX + soundUri.hashCode();
            if (notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Medicine Reminder Custom Sound",
                        NotificationManager.IMPORTANCE_HIGH);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(soundUri, audioAttributes);
                notificationManager.createNotificationChannel(channel);
            }
        }

        return channelId;
    }
}
