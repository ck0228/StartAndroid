package com.android.car.evsafeservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver_EvSafeService";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed event received");

            try {
                // Create explicit intent
                Intent serviceIntent = new Intent(context, EvSafeService.class);
                
                Log.d(TAG, "Starting foreground service");
                context.startForegroundService(serviceIntent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start EvSafeService", e);
            }

        }
    }
}
