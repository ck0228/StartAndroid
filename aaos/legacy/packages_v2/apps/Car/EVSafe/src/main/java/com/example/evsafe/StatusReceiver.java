package com.example.evsafe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StatusReceiver extends BroadcastReceiver {
    private static final String TAG = "StatusReceiver_EVSafe";
    private static final String ACTION_STATUS_UPDATE = "com.android.car.evsafeservice.ACTION_STATUS_UPDATE";
    private static final String PERMISSION_RECEIVE_STATUS = "com.android.car.evsafeservice.permission.RECEIVE_STATUS_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d(TAG, "Received broadcast with action: " + (intent != null ? intent.getAction() : "null"));

            if (!validateBroadcast(context, intent)) {
                return;
            }

            if (context instanceof MainActivity) {
                ((MainActivity) context).handleStatusUpdate(intent);
                Log.d(TAG, "Status update handled by MainActivity");
            } else {
                Log.w(TAG, "Context is not MainActivity: " + context.getClass().getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing broadcast", e);
        }
    }

    private boolean validateBroadcast(Context context, Intent intent) {
        if (context == null) {
            Log.e(TAG, "Null context received");
            return false;
        }

        if (intent == null || intent.getAction() == null) {
            Log.e(TAG, "Invalid intent received");
            return false;
        }

        if (!ACTION_STATUS_UPDATE.equals(intent.getAction())) {
            Log.d(TAG, "Ignoring unrelated action: " + intent.getAction());
            return false;
        }

        return true;
    }
}