package com.example.evsafe;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity_EVSafe";
    static final String ACTION_STATUS_UPDATE = "com.android.car.evsafeservice.ACTION_STATUS_UPDATE";
    static final String PERMISSION_RECEIVE_STATUS = "com.android.car.evsafeservice.permission.RECEIVE_STATUS_UPDATE";

    private static final class Extras {
        static final String BATTERY_STATUS = "batteryStatus";
        static final String BATTERY_LEVEL = "batteryLevel";
        static final String RANGE_STATUS = "rangeStatus";
        static final String SPEED_LEVEL = "speedLevel";
        static final String GEAR_STATUS = "gearStatus";
        static final String SPEED_WARNING = "speedWarning";
    }

    private TextView gearText;
    private TextView batteryText;
    private TextView speedText;
    private TextView rangeText;
    
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isReceiverRegistered = false;
    private StatusReceiver statusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.d(TAG, "onCreate: Starting activity initialization");
        initializeViews();
        checkAndRequestPermissions();
        
        // Initialize receiver
        statusReceiver = new StatusReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerStatusReceiver();
        sendTestBroadcast();
    }

    @Override
    protected void onPause() {
        unregisterStatusReceiver();
        super.onPause();
    }

    // Called by StatusReceiver
    void handleStatusUpdate(Intent intent) {
        try {
            VehicleStatus status = extractVehicleStatus(intent);
            updateUI(status);
            Log.d(TAG, "Status update processed: " + status);
        } catch (Exception e) {
            Log.e(TAG, "Error processing status update", e);
            showError("Failed to process vehicle status");
        }
    }

    private void initializeViews() {
        try {
            gearText = findViewById(R.id.gear_text);
            batteryText = findViewById(R.id.battery_text);
            speedText = findViewById(R.id.speed_text);
            rangeText = findViewById(R.id.range_text);
            
            validateViews();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize views", e);
            showError("Failed to initialize views");
        }
    }

    private void validateViews() {
        if (gearText == null || batteryText == null || 
            speedText == null || rangeText == null) {
            throw new IllegalStateException("Required views not found");
        }
    }

    private void checkAndRequestPermissions() {
        if (checkSelfPermission(PERMISSION_RECEIVE_STATUS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Required permission not granted");
            showError("Missing required permissions");
            finish();
        }
    }

    private void registerStatusReceiver() {
        if (!isReceiverRegistered) {
            try {
                IntentFilter filter = new IntentFilter(ACTION_STATUS_UPDATE);
                registerReceiver(statusReceiver, filter, PERMISSION_RECEIVE_STATUS, null);
                isReceiverRegistered = true;
                Log.d(TAG, "Status receiver registered successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to register receiver", e);
                showError("Failed to initialize status monitoring");
            }
        }
    }

    private void unregisterStatusReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(statusReceiver);
                isReceiverRegistered = false;
                Log.d(TAG, "Status receiver unregistered");
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
    }

    private boolean validateBroadcast(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.w(TAG, "Received invalid broadcast");
            return false;
        }
        
        if (!ACTION_STATUS_UPDATE.equals(intent.getAction())) {
            Log.d(TAG, "Ignoring unrelated broadcast: " + intent.getAction());
            return false;
        }
        
        return true;
    }

    private void processStatusUpdate(Intent intent) {
        try {
            VehicleStatus status = extractVehicleStatus(intent);
            updateUI(status);
            Log.d(TAG, "Status update processed: " + status);
        } catch (Exception e) {
            Log.e(TAG, "Error processing status update", e);
            showError("Failed to process vehicle status");
        }
    }

    private VehicleStatus extractVehicleStatus(Intent intent) {
        return new VehicleStatus(
            intent.getFloatExtra(Extras.BATTERY_STATUS, 0f),
            intent.getFloatExtra(Extras.BATTERY_LEVEL, 0f),
            intent.getFloatExtra(Extras.RANGE_STATUS, 0f),
            intent.getFloatExtra(Extras.SPEED_LEVEL, 0f),
            intent.getStringExtra(Extras.GEAR_STATUS),
            intent.getBooleanExtra(Extras.SPEED_WARNING, false)
        );
    }

    private void updateUI(final VehicleStatus status) {
        mainHandler.post(() -> {
            try {
                gearText.setText(String.format("Gear: %s", status.gearStatus));
                batteryText.setText(String.format("Battery: %.0f%%", status.batteryStatus));
                speedText.setText(String.format("Speed: %.0f km/h", status.speedLevel));
                rangeText.setText(String.format("Range: %.0f km", status.rangeStatus));
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI", e);
                showError("Failed to update display");
            }
        });
    }

    private void showError(final String message) {
        mainHandler.post(() -> 
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );
    }

    private void sendTestBroadcast() {
        Intent testIntent = new Intent(ACTION_STATUS_UPDATE)
            .putExtra(Extras.BATTERY_STATUS, 75.0f)
            .putExtra(Extras.BATTERY_LEVEL, 150000.0f)
            .putExtra(Extras.RANGE_STATUS, 300.0f)
            .putExtra(Extras.SPEED_LEVEL, 0.0f)
            .putExtra(Extras.GEAR_STATUS, "P")
            .putExtra(Extras.SPEED_WARNING, false);
            
        sendBroadcast(testIntent, PERMISSION_RECEIVE_STATUS);
        Log.d(TAG, "Test broadcast sent");
    }

    private static class VehicleStatus {
        final float batteryStatus;
        final float batteryLevel;
        final float rangeStatus;
        final float speedLevel;
        final String gearStatus;
        final boolean speedWarning;

        VehicleStatus(float batteryStatus, float batteryLevel, 
                     float rangeStatus, float speedLevel,
                     String gearStatus, boolean speedWarning) {
            this.batteryStatus = batteryStatus;
            this.batteryLevel = batteryLevel;
            this.rangeStatus = rangeStatus;
            this.speedLevel = speedLevel;
            this.gearStatus = gearStatus != null ? gearStatus : "Unknown";
            this.speedWarning = speedWarning;
        }

        @Override
        public String toString() {
            return String.format("Status[battery=%.1f%%, range=%.1fkm, speed=%.1fkm/h, gear=%s]",
                batteryStatus, rangeStatus, speedLevel, gearStatus);
        }
    }

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!validateBroadcast(intent)) {
                return;
            }
            
            handleStatusUpdate(intent);
        }
    }
}