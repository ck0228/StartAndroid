package com.example.evsafe;

// Android core
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

// Android widgets
import android.widget.TextView;

import android.car.evsafeservice.IEvSafeService;
import android.car.evsafeservice.IEvSafeServiceCallback;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity_EvSafe";
    private static final String ACTION_EV_STATUS_UPDATE = "com.android.car.evsafeservice.ACTION_STATUS_UPDATE";

    // UI elements
    private TextView gearText;
    private TextView batteryText;
    private TextView speedText;
    private TextView rangeText;

    private IEvSafeService evSafeService;

    private final IEvSafeServiceCallback.Stub callback = new IEvSafeServiceCallback.Stub() {
        @Override
        public void onSpeedChanged(int speed) {
            runOnUiThread(() -> updateSpeed(speed));
        }

        @Override
        public void onBatteryChanged(int batteryStatus) {
            runOnUiThread(() -> updateBattery(batteryStatus));
        }

        @Override
        public void onGearChanged(String gear) {
            runOnUiThread(() -> updateGear(gear));
        }

        @Override
        public void onRangeChanged(int range) {
            runOnUiThread(() -> updateRange(range));
        }

        @Override
        public void onEvSafeServiceEvent(int eventType, Bundle eventData) {
            // Handle other events if needed
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            evSafeService = IEvSafeService.Stub.asInterface(service);
            try {
                evSafeService.registerCallback(callback);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to register callback", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            evSafeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_main);
        initializeViews();

        Intent bindIntent = new Intent();
        bindIntent.setAction("com.example.evsafe.BIND_EV_SAFE_SERVICE");
        bindIntent.setComponent(new ComponentName(
            "com.android.car",
            "com.android.car.evsafeservice.EvSafeService"
        ));
        
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Activity setup completed");
    }

    private void initializeViews() {
        Log.d(TAG, "Initializing views");
        gearText = findViewById(R.id.gear_text);
        Log.d(TAG, "gearText found: " + (gearText != null));
        
        batteryText = findViewById(R.id.battery_text);
        Log.d(TAG, "batteryText found: " + (batteryText != null));
        
        speedText = findViewById(R.id.speed_text);
        Log.d(TAG, "speedText found: " + (speedText != null));
        
        rangeText = findViewById(R.id.range_text);
        Log.d(TAG, "rangeText found: " + (rangeText != null));
        
        Log.d(TAG, "Views initialization completed");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        if (evSafeService != null) {
            try {
                evSafeService.unregisterCallback(callback);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to unregister callback", e);
            }
        }
        unbindService(connection);
        super.onDestroy();
    }

    private void updateSpeed(int speed) {
        if (speedText != null) {
            String text = String.format("Speed: %d km/h", speed);
            speedText.setText(text);
            Log.d(TAG, "updateSpeed - Set speed text: " + text);
        }
    }

    private void updateBattery(int batteryStatus) {
        if (batteryText != null) {
            String text = String.format("Battery: %d%%", batteryStatus);
            batteryText.setText(text);
            Log.d(TAG, "updateBattery - Set battery text: " + text);
        }
    }

    private void updateGear(String gear) {
        if (gearText != null) {
            String text = "Gear: " + gear;
            gearText.setText(text);
            Log.d(TAG, "updateGear - Set gear text: " + text);
        }
    }

    private void updateRange(int range) {
        if (rangeText != null) {
            String text = String.format("Range: %d km", range);
            rangeText.setText(text);
            Log.d(TAG, "updateRange - Set range text: " + text);
        }
    }
}