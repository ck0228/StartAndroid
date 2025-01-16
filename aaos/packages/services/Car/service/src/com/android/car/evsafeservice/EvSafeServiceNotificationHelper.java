package com.android.car.evsafeservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

public class EvSafeServiceNotificationHelper {
    public static final String TAG = "EvSafeServiceNotificationHelper";
    private final Context context;
    private final NotificationManager notificationManager;
    private final EvSafeServiceData currentStatus;

    public EvSafeServiceNotificationHelper(Context context, EvSafeServiceData currentStatus) {
        this.context = Objects.requireNonNull(context, "Context cannot be null");
        this.currentStatus = Objects.requireNonNull(currentStatus, "EvSafeServiceStatus cannot be null");
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (this.notificationManager == null) {
            throw new IllegalStateException("NotificationManager not available");
        }
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        try {
            if (notificationManager.getNotificationChannel(EvSafeServiceConfigs.Notification.WARNING_CHANNEL_ID) == null) {
                NotificationChannel warningChannel = new NotificationChannel(
                    EvSafeServiceConfigs.Notification.WARNING_CHANNEL_ID,
                    "EV Safety Warnings",
                    NotificationManager.IMPORTANCE_HIGH);
                
                warningChannel.setDescription("Critical safety warnings for your EV");
                warningChannel.enableVibration(true);
                warningChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
                warningChannel.enableLights(true);
                warningChannel.setLightColor(Color.RED);
                warningChannel.setShowBadge(true);
                warningChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                warningChannel.setBypassDnd(true);
                
                notificationManager.createNotificationChannel(warningChannel);
                Log.d(TAG, "Warning notification channel created successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channel", e);
        }
    }

    public void showWarningNotification(String title, String message) {
        Objects.requireNonNull(title, "Notification title cannot be null");
        Objects.requireNonNull(message, "Notification message cannot be null");
        
        try {
            Log.d(TAG, "Preparing warning notification: " + title + " - " + message);
            
            // Create pending intent for notification tap action
            Intent intent = new Intent(context, EvSafeService.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getService(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Notification notification = new NotificationCompat.Builder(context, 
                    EvSafeServiceConfigs.Notification.WARNING_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // Ensure this icon resource is valid
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .build();

            notificationManager.notify(
                EvSafeServiceConfigs.Notification.WARNING_NOTIFICATION_ID, 
                notification
            );
            Log.d(TAG, "Warning notification sent successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to show warning notification", e);
            throw new RuntimeException("Failed to show notification", e);
        }
    }
}
