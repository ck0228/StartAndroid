// path: /home/chank/aaos/packages/services/Car/service/src/com/android/car/evsafeservice/EvSafeService.java

package com.android.car.evsafeservice;

import android.car.evsafeservice.EvSafeServiceApi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.VehicleGear;
import android.car.hardware.property.CarPropertyManager;
import android.car.hardware.property.CarPropertyEvent;
import android.car.hardware.CarPropertyValue;

import android.os.Handler;
import android.os.Looper;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.app.AppOpsManager;

import java.util.List;
import java.util.Objects;

public class EvSafeService extends Service {
    private static final String TAG = "EvSafeService";

    private EvSafeServiceNotificationHelper notificationHelper;
    private EvSafeServiceVehiclePropertyHandler propertyHandler;
    private Car car;
    private CarPropertyManager propertyManager;
    private EvSafeServiceStatus currentStatus;
    private boolean isServiceRunning;

    private final EvSafeServiceApi.Stub binder = new EvSafeServiceApi.Stub() {
        @Override
        public void startService() throws RemoteException {
            if (currentStatus == null) {
                throw new RemoteException("Service not properly initialized");
            }
            startEvSafeService();
        }

        @Override
        public void stopService() throws RemoteException {
            stopEvSafeService();
        }

        @Override
        public float getBatteryStatus() throws RemoteException {
            return currentStatus.getBatteryStatus();
        }

        @Override
        public float getRangeStatus() throws RemoteException {
            return currentStatus.getRangeStatus();
        }

        @Override
        public float getSpeedStatus() throws RemoteException {
            return currentStatus.getSpeedStatus();
        }

        @Override
        public String getGearStatus() throws RemoteException {
            return currentStatus.getGearStatus();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");

        try {
            initializeComponents();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize service", e);
            stopSelf();
        }
    }

    /**
     * Called to fully initialize car-related components and the service status objects.
     */
    private void initializeComponents() {
        // Init service status
        currentStatus = new EvSafeServiceStatus();
        notificationHelper = new EvSafeServiceNotificationHelper(this, currentStatus);

        try {
            car = Car.createCar(this);
            if (car == null) {
                throw new IllegalStateException("Failed to create Car instance");
            }
            propertyManager = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);
            if (propertyManager == null) {
                throw new IllegalStateException("Failed to get CarPropertyManager");
            }
            propertyHandler = new EvSafeServiceVehiclePropertyHandler(
                    propertyManager,
                    currentStatus,
                    notificationHelper
            );

            Log.d(TAG, "5. Components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize car components", e);
            throw new IllegalStateException("Car initialization failed", e);
        }
    }

    /**
     * Starts the foreground service logic if not already running.
     */
    private synchronized void startEvSafeService() {
        if (isServiceRunning) {
            Log.d(TAG, "Service already running, ignoring start request");
            return;
        }
        if (notificationHelper == null ||
                propertyHandler == null ||
                propertyManager == null ||
                currentStatus == null) {
            Log.e(TAG, "Service components not initialized properly. Cannot start service.");
            return;
        }

        try {
            isServiceRunning = true;
            Log.d(TAG, "6. Starting foreground service and creating notification");

            Notification notification = notificationHelper.createServiceNotification();
            if (notification == null) {
                Log.e(TAG, "Notification is null, cannot start foreground service");
                return;
            }
            startForeground(
                EvSafeServiceConstants.Notification.SERVICE_NOTIFICATION_ID,
                notification
            );
            propertyHandler.registerListeners();
            Log.d(TAG, "7. Service started and running in foreground");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start service", e);
            stopSelf();
        }
    }


    private synchronized void stopEvSafeService() {
        if (isServiceRunning) {
            try {
                isServiceRunning = false;
                if (propertyHandler != null) {
                    propertyHandler.unregisterListeners();
                }
                stopForeground(true);
            } catch (Exception e) {
                Log.e(TAG, "Error stopping service", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        cleanup();
        super.onDestroy();
    }

    private void cleanup() {
        try {
            stopEvSafeService();
            if (car != null) {
                car.disconnect();
                car = null;
            }

            propertyManager = null;
            propertyHandler = null;
            notificationHelper = null;
            currentStatus = null;
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "1. Service onStartCommand");

        if (notificationHelper == null) {
            // Create a temp placeholder for the first-time start
            currentStatus = new EvSafeServiceStatus();
            notificationHelper = new EvSafeServiceNotificationHelper(this, currentStatus);
        } else {
            Log.d(TAG, "NotificationHelper already initialized");
        }

        if (!isServiceRunning) {
            Notification notification = notificationHelper.createServiceNotification();
            if (notification == null) {
                Log.e(TAG, "Failed to create notification");
            } else {
                Log.d(TAG, "2. Notification created successfully");
                Log.d(TAG, "3. Calling startForeground");
                startForeground(EvSafeServiceConstants.Notification.SERVICE_NOTIFICATION_ID, notification);
            }
        }

        try {
            if (car == null || propertyManager == null || propertyHandler == null) {
                Log.d(TAG, "4. initializing components");
                initializeComponents();
            } else {
                Log.d(TAG, "4. All components already initialized");
            }
            startEvSafeService();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            stopSelf();
        }

        return START_STICKY;
    }
}