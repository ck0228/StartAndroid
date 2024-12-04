package com.example.evsafeservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.evsafeservice.EvSafeService;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed event received");
            Intent serviceIntent = new Intent(context, EvSafeService.class);
            context.startService(serviceIntent);
        }
    }
}
