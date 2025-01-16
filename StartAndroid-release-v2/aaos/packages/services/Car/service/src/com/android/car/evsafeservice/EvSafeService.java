package com.android.car.evsafeservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import android.car.Car;
import android.car.VehicleGear;
import android.car.VehiclePropertyIds;
import android.car.hardware.property.CarPropertyManager;
import android.car.hardware.property.CarPropertyEvent;
import android.car.hardware.CarPropertyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.car.evsafeservice.IEvSafeService;
import android.car.evsafeservice.IEvSafeServiceCallback;

public class EvSafeService extends Service {
    private static final String TAG = "EvSafeService";

    private static final class Properties {
        static final String SPEED = "speed";
        static final String BATTERY = "battery";
        static final String GEAR = "gear";
        static final String RANGE = "range";
    }
    
    private Car car;
    private CarPropertyManager carPropertyManager;
    private final List<IEvSafeServiceCallback> callbacks = new ArrayList<>();
    private EvSafeServiceDataManager dataManager;
    private EvSafeServiceNotificationHelper notificationHelper;
    private boolean isServiceReady = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Starting EvSafeService initialization");
        
        try {
            initializeCar();
            initializeComponents();
            isServiceReady = true;
            Log.i(TAG, "EvSafeService initialization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize EvSafeService", e);
            stopSelf();
        }
    }

    private void initializeCar() {
        Log.d(TAG, "Initializing Car instance");
        car = Car.createCar(this);
        if (car == null) {
            throw new IllegalStateException("Failed to create Car instance");
        }
        
        carPropertyManager = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);
        if (carPropertyManager == null) {
            throw new IllegalStateException("Failed to get CarPropertyManager");
        }
        Log.d(TAG, "Car initialization completed");
    }

    private void initializeComponents() {
        Log.d(TAG, "Initializing service components");
        
        EvSafeServiceData evSafeServiceData = new EvSafeServiceData();
        notificationHelper = new EvSafeServiceNotificationHelper(this, evSafeServiceData);
        
        dataManager = new EvSafeServiceDataManager(
            carPropertyManager, 
            evSafeServiceData, 
            new EvSafeServiceDataUpdateListener());
            
        dataManager.registerListeners();
        Log.d(TAG, "Components initialization completed");
    }
        
    private void updateMetrics(EvSafeServiceData data) {
        notifyPropertyChanged(Properties.SPEED, (int) data.getSpeed());
        notifyPropertyChanged(Properties.BATTERY, (int) data.getBatteryStatus());
        notifyPropertyChanged(Properties.GEAR, data.getGearStatus());
        notifyPropertyChanged(Properties.RANGE, (int) data.getRange());
    }

    private void checkWarnings(EvSafeServiceData data) {
        if (data.getSpeedWarning()) {
            notificationHelper.showWarningNotification(
                "Speed Warning", 
                "Your speed exceeds the safe limit!"
            );
        }
    }

    private class EvSafeServiceDataUpdateListener 
            implements EvSafeServiceDataManager.EvSafeServiceDataUpdateListener {
        @Override
        public void onEvSafeServiceDataUpdated(EvSafeServiceData data) {
            try {
                updateMetrics(data);
                checkWarnings(data);
            } catch (Exception e) {
                Log.e(TAG, "Error processing data update", e);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.i(TAG, "Intent action: " + intent.getAction());
            Log.i(TAG, "Intent component: " + (intent.getComponent() != null ? intent.getComponent().getClassName() : "Unknown"));
        } else {
            // Create an intent to bind the service
            Intent bindIntent = new Intent(this, EvSafeService.class);
            bindIntent.setAction("com.example.evsafe.BIND_EV_SAFE_SERVICE");
            startService(bindIntent);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind called");
        if (intent != null) {
            String serviceName = intent.getComponent() != null ? intent.getComponent().getClassName() : "Unknown";
            Log.d(TAG, "Binding to service: " + serviceName);
            Log.d(TAG, "Intent action: " + intent.getAction());
        }
        return binderInterface;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service being destroyed - cleaning up resources");
        try {
            if (dataManager != null) {
                dataManager.unregisterListeners();
            }
            if (car != null) {
                car.disconnect();
            }
            callbacks.clear();
        } catch (Exception e) {
            Log.e(TAG, "Error during service cleanup", e);
        }
        isServiceReady = false;
        super.onDestroy();
    }

    private final IEvSafeService.Stub binderInterface = new IEvSafeService.Stub() {
        @Override
        public void registerCallback(IEvSafeServiceCallback callback) {
            if (callback != null) {
                callbacks.add(callback);
            }
        }
    
        @Override
        public void unregisterCallback(IEvSafeServiceCallback callback) {
            if (callback != null) {
                callbacks.remove(callback);
            }
        }
    
        @Override
        public boolean isServiceReady() {
            return isServiceReady;
        }
    };

    private void notifyPropertyChanged(String property, Object value) {
        if (value == null) return;
        
        Log.d(TAG, String.format("Notifying property change - %s: %s", property, value));
        
        for (IEvSafeServiceCallback callback : new ArrayList<>(callbacks)) {
            try {
                switch (property) {
                    case Properties.SPEED:
                        callback.onSpeedChanged((int) value);
                        break;
                    case Properties.BATTERY:
                        callback.onBatteryChanged((int) value);
                        break;
                    case Properties.GEAR:
                        callback.onGearChanged((String) value);
                        break;
                    case Properties.RANGE:
                        callback.onRangeChanged((int) value);
                        break;
                    default:
                        Log.w(TAG, "Unknown property: " + property);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Remote exception while notifying callback", e);
                callbacks.remove(callback);
            }
        }
    }
}