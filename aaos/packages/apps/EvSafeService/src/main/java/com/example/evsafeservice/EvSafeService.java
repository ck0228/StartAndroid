package com.example.evsafeservice;

import android.app.Service;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.VehicleGear;
import android.car.hardware.property.CarPropertyManager;
import android.os.Handler;

public class EvSafeService extends Service {
    // Constants
    private static final String TAG = "EVSafeService";
    private static final int UPDATE_INTERVAL_MS = 1000;
    private String gearStatus;
    private float speedStatus;
    private float batteryStatus;
    private float rangeStatus;

    // Car related members
    private Car car;
    private CarPropertyManager propertyManager;
    private Handler handler;

    // Update handler
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateVehicleStatus();
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "EvSafeService started");
        startForeground(1, getNotification());
        handler = new Handler();
        initCar();
        handler.post(updateRunnable);
    }

    private Notification getNotification() {
        Log.d(TAG, "EvSafeService Notification for foreground");
        return null;
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
    
        gearStatus = updateGearStatus();
        speedStatus = updateSpeedStatus();
        batteryStatus = updateBatteryStatus();
        rangeStatus = updateRangeStatus(batteryStatus, speedStatus);
    }

    private String updateGearStatus() {
        String gear = "Unknown";

        try {
            int gearInt = propertyManager.getIntProperty(
                VehiclePropertyIds.GEAR_SELECTION,
                0
            );
            return gearString(gearInt);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for gear reading", e);
            return gear;
        } catch (Exception e) {
            Log.e(TAG, "Error reading gear", e);
            return gear;
        }
    }

    private String gearString(int gearInt) {
        switch (gearInt) {
            case VehicleGear.GEAR_PARK: return "P";
            case VehicleGear.GEAR_REVERSE: return "R";
            case VehicleGear.GEAR_NEUTRAL: return "N";
            case VehicleGear.GEAR_DRIVE: return "D";
            default: return "Unknown"; 
        }
    }
    
    private float updateSpeedStatus() {
        float speedKmh = -1;

        try {
            float speedMs = propertyManager.getFloatProperty(
                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                0
            );
            speedKmh = convertMsToKmh(speedMs);

        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for speed reading", e);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Error reading speed", e);
            return -1;
        }
        return speedKmh;
    }

    private float convertMsToKmh(float speedMs) {
        return speedMs * 3.6f; // m/s * (3600/1000) = km/h
    }

    private float updateBatteryStatus() {
        float batteryPercentage = -1;

        try {
            float batteryLevel = propertyManager.getFloatProperty(
                VehiclePropertyIds.EV_BATTERY_LEVEL,
                0
            );
            float batteryCapacity = propertyManager.getFloatProperty(
                VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY,
                0
            );
            
            batteryPercentage = calculateBatteryPercentage(batteryLevel, batteryCapacity);
            
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for battery reading", e);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Error reading battery", e);
            return -1;
        }
        return batteryPercentage;
    }

    private float calculateBatteryPercentage(float batteryLevel, float batteryCapacity) {
        if (batteryCapacity <= 0) {
            Log.w(TAG, "Invalid battery capacity: " + batteryCapacity);
            return 0.0f;
        }
        return (batteryLevel / batteryCapacity) * 100.0f;
    }

        // 배터리와 속도에 따른 주행 가능 거리 계산 함수
        private float updateRangeStatus(float batteryPercentage, float currentSpeed) {
            final float MAX_RANGE = 450.0f; // 100%일 때 최대 주행거리 (km)
            float speedFactor = calculateSpeedFactor(currentSpeed); // 속도에 따른 가중치 계산
            return (batteryPercentage / 100.0f) * MAX_RANGE * speedFactor;
        }
    
        // 속도에 따른 가중치 계산 함수
        private float calculateSpeedFactor(float speed) {
            if (speed < 80) {
                return 1.0f;
            } else if (speed <= 100) {
                return 0.7f;
            } else {
                return 0.5f;
            }
        }
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Keep service running
    }

    @Override
    public void onDestroy() {
        if (car != null) {
            car.disconnect();
        }
        handler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}