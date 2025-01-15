package com.android.car.evsafeservice;

// Android core
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

// Android car
import android.car.Car;
import android.car.VehicleGear;
import android.car.VehiclePropertyIds;
import android.car.hardware.property.CarPropertyManager;
import android.car.hardware.property.CarPropertyEvent;
import android.car.hardware.CarPropertyValue;

// Java utils
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EvSafeService extends Service {
    private static final String TAG = "EvSafeService";
    
    private final IBinder binder = new LocalBinder();
    private final List<IEvSafeServiceCallback> callbacks = new ArrayList<>();
    private EvSafeServiceDataManager dataManager;
    private EvSafeServiceNotificationHelper notificationHelper;
    private boolean isServiceReady = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        // Initialize data manager and other components here
        EvSafeServiceData evSafeServiceData = new EvSafeServiceData();
        notificationHelper = new EvSafeServiceNotificationHelper(this, evSafeServiceData);
        dataManager = new EvSafeServiceDataManager(new CarPropertyManager(), evSafeServiceData, new EvSafeServiceDataManager.EvSafeServiceDataUpdateListener() {
            @Override
            public void onEvSafeServiceDataUpdated(EvSafeServiceData evSafeServiceData) {
                // Handle updated data
                notifyPropertyChanged("speed", (int) evSafeServiceData.getSpeed());
                notifyPropertyChanged("battery", (int) evSafeServiceData.getBatteryStatus());
                notifyPropertyChanged("gear", evSafeServiceData.getGearStatus());
                notifyPropertyChanged("range", (int) evSafeServiceData.getRange());

                // Check for speed warning and send notification
                if (evSafeServiceData.getSpeedWarning()) {
                    notificationHelper.showWarningNotification("Speed Warning", "Your speed exceeds the safe limit!");
                }
            }
        });
        dataManager.registerListeners();
        isServiceReady = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        // Clean up resources here
        dataManager.unregisterListeners();
        isServiceReady = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        EvSafeService getService() {
            return EvSafeService.this;
        }
    }

    private final IEvSafeService.Stub binderInterface = new IEvSafeService.Stub() {
        @Override
        public void registerCallback(IEvSafeServiceCallback callback) {
            if (callback != null) {
                callbacks.add(callback);
                Log.d(TAG, "Callback registered");
            }
        }

        @Override
        public void unregisterCallback(IEvSafeServiceCallback callback) {
            if (callback != null) {
                callbacks.remove(callback);
                Log.d(TAG, "Callback unregistered");
            }
        }

        @Override
        public void updateEvSafeServiceConfig(Bundle config) {
            // Handle configuration update
            Log.d(TAG, "Configuration updated");
        }

        @Override
        public boolean isServiceReady() {
            return isServiceReady;
        }
    };

    private void notifyPropertyChanged(String property, Object value) {
        for (IEvSafeServiceCallback callback : callbacks) {
            try {
                switch (property) {
                    case "speed":
                        callback.onSpeedChanged((int) value);
                        break;
                    case "battery":
                        callback.onBatteryChanged((int) value);
                        break;
                    case "gear":
                        callback.onGearChanged((String) value);
                        break;
                    case "range":
                        callback.onRangeChanged((int) value);
                        break;
                    default:
                        Log.e(TAG, "Unknown property: " + property);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error notifying property change: " + property, e);
            }
        }
    }

    private void notifyEvSafeServiceEvent(int eventType, Bundle eventData) {
        for (IEvSafeServiceCallback callback : callbacks) {
            try {
                callback.onEvSafeServiceEvent(eventType, eventData);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying service event", e);
            }
        }
    }
}