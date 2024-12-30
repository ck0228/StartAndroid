// NotificationHelper.java
package com.android.car.evsafeservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;


public class EvSafeServiceNotificationHelper {
    public static final String TAG = "EvSafeServiceNotificationHelper";
    private final Context context;
    private final NotificationManager notificationManager;
    private final EvSafeServiceStatus currentStatus;
    private String lastStatus;

    public EvSafeServiceNotificationHelper(Context context, EvSafeServiceStatus currentStatus) {
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
            NotificationChannel warningChannel = new NotificationChannel(
                EvSafeServiceConstants.Notification.WARNING_CHANNEL_ID,
                "EvSafeWarning Channel",
                NotificationManager.IMPORTANCE_HIGH);
            warningChannel.setDescription("Shows important Ev warnings");
            warningChannel.enableVibration(true);
            
            notificationManager.createNotificationChannel(warningChannel);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channel", e);
        }
    }

    public void showWarningNotification(String title, String message) {
        Objects.requireNonNull(title, "Notification title cannot be null");
        Objects.requireNonNull(message, "Notification message cannot be null");
        
        try {
            Log.d(TAG, "Showing warning notification: " + title + " - " + message);
            Notification notification = new Notification.Builder(context, 
                EvSafeServiceConstants.Notification.WARNING_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

            notificationManager.notify(EvSafeServiceConstants.Notification.WARNING_NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.e(TAG, "Failed to show warning notification", e);
        }
    }
}