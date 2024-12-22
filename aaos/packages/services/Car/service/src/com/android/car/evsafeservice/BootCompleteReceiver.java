// path: /home/chank/aaos/packages/services/Car/service/src/com/android/car/evsafeservice/BootCompleteReceiver.java

package com.android.car.evsafeservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver_EvSafeService";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) {
            Log.w(TAG, "Received null context, intent or action");
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed event received");
            startEvSafeService(context);
        }
    }

    private void startEvSafeService(Context context) {
        try {
            Intent serviceIntent = new Intent(context, EvSafeService.class);
            Log.d(TAG, "Starting foreground service");
            context.startForegroundService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start EvSafeService", e);
        }
    }
}