// NotificationHelper.java
package com.android.car.evsafeservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import java.util.Objects;

/**
 * Helper class for managing EV Safe Service notifications.
 * Handles notification channel creation and warning notifications.
 */
public class EvSafeServiceNotificationHelper {
    public static final String TAG = "EvSafeServiceNotificationHelper";

    private String lastNotificationKey;
    private final Context context;
    private final NotificationManager notificationManager;
    private final EvSafeServiceStatus currentStatus;

    /**
     * Creates a new notification helper instance
     * @param context Application context
     * @param currentStatus Current vehicle status
     * @throws IllegalStateException if notification manager is unavailable
     */
    public EvSafeServiceNotificationHelper(Context context, EvSafeServiceStatus currentStatus) {
        this.context = Objects.requireNonNull(context, "Context cannot be null");
        this.currentStatus = Objects.requireNonNull(currentStatus, "EvSafeServiceStatus cannot be null");
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (this.notificationManager == null) {
            throw new IllegalStateException("NotificationManager not available");
        }
        
        initializeNotificationChannel();
    }
    
    /**
     * Creates and configures the warning notification channel
     */
    private void initializeNotificationChannel() {
        try {
            NotificationChannel warningChannel = new NotificationChannel(
                EvSafeServiceConstants.Notification.WARNING_CHANNEL_ID,
                EvSafeServiceConstants.Notification.TITLE,
                NotificationManager.IMPORTANCE_HIGH);
                
            warningChannel.setDescription(EvSafeServiceConstants.Notification.DESCRIPTION);
            warningChannel.enableVibration(true);
            warningChannel.setShowBadge(true);
            
            notificationManager.createNotificationChannel(warningChannel);
            Log.d(TAG, "Notification channel created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channel", e);
        }
    }

    /**
     * Shows a warning notification if not already displayed
     * @param title Notification title
     * @param message Notification message
     */
    public void showWarningNotification(String title, String message) {
        Objects.requireNonNull(title, "Notification title cannot be null");
        Objects.requireNonNull(message, "Notification message cannot be null");
        
        // Prevent duplicate notifications
        String notificationKey = title + message;
        if (notificationKey.equals(lastNotificationKey)) {
            Log.d(TAG, "Skipping duplicate notification");
            return;
        }
        
        try {
            Notification notification = buildWarningNotification(title, message);
            notificationManager.notify(
                EvSafeServiceConstants.Notification.WARNING_NOTIFICATION_ID, 
                notification
            );
            
            lastNotificationKey = notificationKey;
            Log.d(TAG, "Warning notification shown: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Failed to show warning notification", e);
        }
    }

    /**
     * Builds a warning notification with specified content
     */
    private Notification buildWarningNotification(String title, String message) {
        return new Notification.Builder(context, 
                EvSafeServiceConstants.Notification.WARNING_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_CAR_EMERGENCY)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build();
    }

    /**
     * Cancels all active notifications
     */
    public void cancelAllNotifications() {
        try {
            notificationManager.cancelAll();
            lastNotificationKey = null;
            Log.d(TAG, "All notifications cancelled");
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel notifications", e);
        }
    }
}