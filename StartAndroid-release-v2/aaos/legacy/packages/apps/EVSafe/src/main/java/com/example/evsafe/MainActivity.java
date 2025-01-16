package com.example.evsafe;

import android.app.Activity;
import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.VehicleGear;
import android.car.hardware.property.CarPropertyManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
    // Constants
    private static final String TAG = "EVSafe";
    private static final int UPDATE_INTERVAL_MS = 1000;

    // Car related members
    private Car car;
    private CarPropertyManager propertyManager;
    
    // UI elements
    private TextView gearText;
    private TextView batteryText;
    private TextView speedText;

    
    
    // Update handler
    private Handler handler;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateVehicleStatus();
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        handler = new Handler(Looper.getMainLooper());
        initViews();
        initCar();
    }

    private void initViews() {
        gearText = findViewById(R.id.gear_status);
        batteryText = findViewById(R.id.battery_status);
        speedText = findViewById(R.id.speed_status);
    }

    private void initCar() {
        try {
            car = Car.createCar(this);
            propertyManager = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create car manager", e);
        }
    }

    private void updateVehicleStatus() {
        if (propertyManager == null) {
            Log.e(TAG, "PropertyManager is null");
            return;
        }
    
        updateGearStatus();
        updateSpeedStatus();
        updateBatteryStatus();
    }
    
    private void updateGearStatus() {
        try {
            int gear = propertyManager.getIntProperty(
                VehiclePropertyIds.GEAR_SELECTION, 
                0
            );
            gearText.setText("Gear: " + getGearString(gear));
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for gear reading", e);
            gearText.setText("Gear: No permission");
        } catch (Exception e) {
            Log.e(TAG, "Error reading gear", e);
            gearText.setText("Gear: Error");
        }
    }
    
    private void updateSpeedStatus() {
        try {
            float speedMs = propertyManager.getFloatProperty(
                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                0
            );
            float speedKmh = convertMsToKmh(speedMs);
            Log.d(TAG, "Speed(m/s): " + speedMs + " -> Speed(km/h): " + speedKmh);
            speedText.setText(String.format("Speed: %.1f km/h", speedKmh));
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for speed reading", e);
            speedText.setText("Speed: No permission");
        } catch (Exception e) {
            Log.e(TAG, "Error reading speed", e);
            speedText.setText("Speed: Error");
        }
    }
    
    private void updateBatteryStatus() {
        try {
            float batteryLevel = propertyManager.getFloatProperty(
                VehiclePropertyIds.EV_BATTERY_LEVEL,
                0
            );
            float batteryCapacity = propertyManager.getFloatProperty(
                VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY,
                0
            );
            
            float batteryPercentage = calculateBatteryPercentage(batteryLevel, batteryCapacity);
            Log.d(TAG, "Battery Level: " + batteryLevel + 
                  " Capacity: " + batteryCapacity + 
                  " Percentage: " + batteryPercentage + "%");
            
            batteryText.setText(String.format("Battery: %.1f%%", batteryPercentage));
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for battery reading", e);
            batteryText.setText("Battery: No permission");
        } catch (Exception e) {
            Log.e(TAG, "Error reading battery", e);
            batteryText.setText("Battery: Error");
        }
    }

    private String getGearString(int gear) {
        if (gear == VehicleGear.GEAR_PARK) return "P";
        if (gear == VehicleGear.GEAR_REVERSE) return "R";
        if (gear == VehicleGear.GEAR_NEUTRAL) return "N";
        if (gear == VehicleGear.GEAR_DRIVE) return "D";
        return "Unknown";
    }

    private float convertMsToKmh(float speedMs) {
        return speedMs * 3.6f; // m/s * (3600/1000) = km/h
    }

    private float calculateBatteryPercentage(float batteryLevel, float batteryCapacity) {
        if (batteryCapacity <= 0) {
            Log.w(TAG, "Invalid battery capacity: " + batteryCapacity);
            return 0.0f;
        }
        return (batteryLevel / batteryCapacity) * 100.0f;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onDestroy() {
        if (car != null) {
            car.disconnect();
        }
        super.onDestroy();
    }
}