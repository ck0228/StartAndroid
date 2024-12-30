package com.example.evsafe;

// Android core
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

// Android widgets
import android.widget.TextView;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity_EvSafe";
    private static final String ACTION_EV_STATUS_UPDATE = "com.android.car.evsafeservice.ACTION_STATUS_UPDATE";

    // UI elements
    private TextView gearText;
    private TextView batteryText;
    private TextView speedText;
    private TextView rangeText;

    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive - Received broadcast");
                
            if (ACTION_EV_STATUS_UPDATE.equals(intent.getAction())) {
                Log.d(TAG, "onReceive - Action matches");
                float batteryStatus = intent.getFloatExtra("batteryStatus", 0f);
                float batteryLevel = intent.getFloatExtra("batteryLevel", 0f);
                float rangeStatus = intent.getFloatExtra("rangeStatus", 0f);
                float speedLevel = intent.getFloatExtra("speedLevel", 0f);
                String gearStatus = intent.getStringExtra("gearStatus");
                boolean speedWarning = intent.getBooleanExtra("speedWarning", false);
    
                Log.d(TAG, String.format("onReceive - Got values: Battery: %.1f%%, Level: %.1f, Range: %.1f, Speed: %.1f, Gear: %s",
                    batteryStatus, batteryLevel, rangeStatus, speedLevel, gearStatus));
    
                updateUI(batteryStatus, batteryLevel, rangeStatus, speedLevel, gearStatus, speedWarning);
            } else {
                Log.d(TAG, "onReceive - Action does not match: " + intent.getAction());
            }
        }
    };

    private void updateUI(float batteryStatus, float batteryLevel,
                     float rangeStatus, float speedLevel,
                     String gearStatus, boolean speedWarning) {
        Log.d(TAG, "updateUI - Starting update");
        try {
            Log.d(TAG, String.format("updateUI - Views: gear=%s, battery=%s, speed=%s, range=%s",
                gearText != null ? "found" : "null",
                batteryText != null ? "found" : "null", 
                speedText != null ? "found" : "null",
                rangeText != null ? "found" : "null"));

            if (gearText != null) {
                String text = "Gear: " + gearStatus;
                gearText.setText(text);
                Log.d(TAG, "updateUI - Set gear text: " + text);
            }
            
            if (batteryText != null) {
                String text = String.format("Battery: %.0f%%", batteryStatus);
                batteryText.setText(text);
                Log.d(TAG, "updateUI - Set battery text: " + text);
            }
            
            if (speedText != null) {
                String text = String.format("Speed: %.0f km/h", speedLevel);
                speedText.setText(text);
                Log.d(TAG, "updateUI - Set speed text: " + text);
            }
            
            if (rangeText != null) {
                String text = String.format("Range: %.0f km", rangeStatus);
                rangeText.setText(text);
                Log.d(TAG, "updateUI - Set range text: " + text);
            }

            Log.d(TAG, "updateUI - Completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "updateUI - Error updating UI", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_main);
        initializeViews();
        
        // Register receiver first
        Log.d(TAG, "Registering broadcast receiver");
        registerReceiver(statusReceiver, new IntentFilter(ACTION_EV_STATUS_UPDATE));
        
        // Send test broadcast after registration
        Log.d(TAG, "Preparing test broadcast");
        Intent testIntent = new Intent(ACTION_EV_STATUS_UPDATE);
        testIntent.putExtra("batteryStatus", 75.0f);
        testIntent.putExtra("batteryLevel", 150000.0f);
        testIntent.putExtra("rangeStatus", 300.0f);
        testIntent.putExtra("speedLevel", 0.0f);
        testIntent.putExtra("gearStatus", "P");
        testIntent.putExtra("speedWarning", false);
        
        Log.d(TAG, "Sending test broadcast");
        sendBroadcast(testIntent);
        
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
        unregisterReceiver(statusReceiver);
        super.onDestroy();
    }
}