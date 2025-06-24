package com.example.mednotifyplus.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;

public class StopReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat.from(context).cancel(1001);
        // Optionally, stop sound if using a service.
    }
}
