// path: /home/chank/aaos/packages/services/Car/service/src/com/android/car/evsafeservice/BootCompleteReceiver.java

package com.android.car.evsafeservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BroadcastReceiver that starts EvSafeService when device boot completes.
 * Ensures only one instance of the service is running.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver_EvSafeService";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (context == null || intent == null || intent.getAction() == null) {
                Log.w(TAG, "Invalid input received");
                return;
            }

            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                synchronized (BootCompleteReceiver.class) {
                    if (isServiceRunning(context)) {
                        Log.d(TAG, "Service already running");
                        return;
                    }
                    Log.d(TAG, "1. Boot completed event received");
                    startEvSafeService(context);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in BootCompleteReceiver", e);
        }
    }

    private boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (EvSafeService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void startEvSafeService(Context context) {
        try {
            Intent serviceIntent = new Intent(context, EvSafeService.class);
            serviceIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            Log.d(TAG, "2. Starting system service");
            context.startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start EvSafeService", e);
        }
    }
}