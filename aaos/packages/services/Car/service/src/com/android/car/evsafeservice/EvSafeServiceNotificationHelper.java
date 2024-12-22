// NotificationHelper.java
package com.android.car.evsafeservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.car.VehicleGear;

import java.util.Objects;


public class EvSafeServiceNotificationHelper {
    public static final String TAG = "EvSafeServiceNotificationHelper";
    private final Context context;
    private final NotificationManager notificationManager;
    private final EvSafeServiceStatus currentStatus;

    public EvSafeServiceNotificationHelper(Context context, EvSafeServiceStatus currentStatus) {
        this.context = Objects.requireNonNull(context, "Context cannot be null");
        this.currentStatus = Objects.requireNonNull(currentStatus, "EvSafeServiceStatus cannot be null");
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (this.notificationManager == null) {
            throw new IllegalStateException("NotificationManager not available");
        }
        createNotificationChannels();
    }
    
    private void createNotificationChannels() {
        // 채널이 이미 존재할 경우 예외가 발생하므로 필요 시 예외 처리 추가 가능
        try {
            NotificationChannel serviceChannel = new NotificationChannel(
                EvSafeServiceConstants.Notification.SERVICE_CHANNEL_ID,
                "Ev Safe Service Channel",
                NotificationManager.IMPORTANCE_LOW);
            serviceChannel.setDescription("Shows ongoing Ev monitoring status");

            NotificationChannel warningChannel = new NotificationChannel(
                EvSafeServiceConstants.Notification.WARNING_CHANNEL_ID,
                "Ev Safe Warning Channel",
                NotificationManager.IMPORTANCE_HIGH);
            warningChannel.setDescription("Shows important Ev warnings");
            warningChannel.enableVibration(true);

            notificationManager.createNotificationChannel(serviceChannel);
            notificationManager.createNotificationChannel(warningChannel);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channels", e);
        }
    }

    public Notification createServiceNotification() {
        Intent notificationIntent = new Intent(context, EvSafeService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    
        return new Notification.Builder(context, EvSafeServiceConstants.Notification.SERVICE_CHANNEL_ID)
                .setContentTitle("Ev Safe Service")
                .setContentText(currentStatus.toString())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .build();
    }

    public void showWarningNotification(String title, String message) {
        Objects.requireNonNull(title, "Notification title cannot be null");
        Objects.requireNonNull(message, "Notification message cannot be null");
        
        try {
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

    public void updateServiceNotification() {
        try {
            notificationManager.notify(
                EvSafeServiceConstants.Notification.SERVICE_NOTIFICATION_ID, 
                createServiceNotification());
        } catch (Exception e) {
            Log.e(TAG, "Failed to update service notification", e);
        }
    }
}