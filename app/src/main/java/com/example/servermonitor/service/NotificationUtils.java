package com.example.servermonitor.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.servermonitor.MainActivity;
import com.example.servermonitor.R;

public class NotificationUtils {
    public static int notificationId = 0;
    private static final String CHANNEL_ID = "my_channel_id";
    public static NotificationChannel channel;

    public static void showNotification(MainActivity activity, Context context, String title, String message) {
        if (channel == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "server_monitor";
                String description = "server_monitor_channel";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationId, builder.build());
    }
}