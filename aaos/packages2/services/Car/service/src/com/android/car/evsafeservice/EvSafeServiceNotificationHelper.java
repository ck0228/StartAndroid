package com.android.car.evsafeservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
                    "EvSafeWarning Channel",
                    NotificationManager.IMPORTANCE_HIGH);
                warningChannel.setDescription("Shows important Ev warnings");
                warningChannel.enableVibration(true);
                
                notificationManager.createNotificationChannel(warningChannel);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channel", e);
        }
    }

    public void showWarningNotification(String title, String message) {
        Objects.requireNonNull(title, "Notification title cannot be null");
        Objects.requireNonNull(message, "Notification message cannot be null");
        
        try {
            Log.d(TAG, "Showing warning notification: " + title + " - " + message);
            Notification notification = new NotificationCompat.Builder(context, 
                EvSafeServiceConfigs.Notification.WARNING_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_dialog_alert) // 애플리케이션 리소스에서 아이콘을 가져옴
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

            notificationManager.notify(EvSafeServiceConfigs.Notification.WARNING_NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.e(TAG, "Failed to show warning notification", e);
        }
    }
}