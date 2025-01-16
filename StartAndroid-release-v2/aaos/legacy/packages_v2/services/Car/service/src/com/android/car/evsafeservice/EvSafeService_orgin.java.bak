package com.android.car.evsafeservice;

// Android core
import android.app.Service;
import android.content.Intent;
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

// Android car service
import android.car.evsafeservice.EvSafeServiceApi;
import android.car.evsafeservice.EvSafeServiceStatusListener;

// Java utils
import java.util.Objects;

/**
 * Service that monitors electric vehicle status and broadcasts updates.
 * Implements singleton pattern to ensure only one instance runs.
 */
public class EvSafeService extends Service {
    private static final String TAG = "EvSafeService";
    private static final String ACTION_EV_STATUS_UPDATE = "com.android.car.evsafeservice.ACTION_STATUS_UPDATE";
    
    // Singleton instance
    private static EvSafeService instance = null;

    // Service state
    private boolean isServiceRunning;
    private boolean broadcastsEnabled;

    // Car components
    private Car car;
    private CarPropertyManager propertyManager;
    private EvSafeServiceVehiclePropertyHandler propertyHandler;
    
    // Status and notifications
    private EvSafeServiceStatus currentStatus;
    private EvSafeServiceNotificationHelper notificationHelper;
    private EvSafeServiceStatusListener statusListener;

    // Broadcast status updates
    private void broadcastStatus() {
        if (!broadcastsEnabled) {
            Log.d(TAG, "Broadcasts disabled, skipping update");
            return;
        }
    
        try {
            Intent intent = new Intent(ACTION_EV_STATUS_UPDATE);
            intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            
            // Add values
            intent.putExtra("batteryStatus", currentStatus.getBatteryStatus());
            intent.putExtra("batteryLevel", currentStatus.getBatteryLevel());
            intent.putExtra("rangeStatus", currentStatus.getRangeStatus());
            intent.putExtra("speedLevel", currentStatus.getspeedLevel());
            intent.putExtra("gearStatus", currentStatus.getGearStatus());
            intent.putExtra("speedWarning", currentStatus.getSpeedWarning());
            
            Log.d(TAG, String.format("Broadcasting values - Speed: %.1f, Gear: %s", 
                currentStatus.getspeedLevel(), currentStatus.getGearStatus()));
                
            // Send to all users
            sendBroadcastAsUser(intent, android.os.UserHandle.ALL);
        } catch (Exception e) {
            Log.e(TAG, "Failed to broadcast status", e);
        }
    }

    // AIDL interface implementation
    private final EvSafeServiceApi.Stub binder = new EvSafeServiceApi.Stub() {
        @Override
        public void startService() throws RemoteException {
            Objects.requireNonNull(currentStatus, "Service not properly initialized");
            broadcastsEnabled = true;
            startEvSafeService();
        }

        @Override
        public void stopService() throws RemoteException {
            stopEvSafeService();
        }

        @Override
        public void registerListener(EvSafeServiceStatusListener listener) throws RemoteException {
            if (listener != null) {
                statusListener = listener;
                Log.d(TAG, "Status listener registered");
            }
        }
    
        @Override
        public void unregisterListener(EvSafeServiceStatusListener listener) throws RemoteException {
            if (statusListener != null && statusListener.equals(listener)) {
                statusListener = null;
                Log.d(TAG, "Status listener unregistered");
            }
        }

        // Broadcast control
        @Override
        public void enableBroadcasts() throws RemoteException {
            broadcastsEnabled = true;
            Log.d(TAG, "Broadcasts enabled");
            broadcastStatus();
        }

        @Override
        public void disableBroadcasts() throws RemoteException {
            broadcastsEnabled = false;
            Log.d(TAG, "Broadcasts disabled");
        }

        // Status getters
        @Override
        public float getBatteryStatus() throws RemoteException {
            return currentStatus.getBatteryStatus();
        }

        @Override
        public float getBatteryLevel() throws RemoteException {
            return currentStatus.getBatteryLevel();
        }

        @Override
        public float getRangeStatus() throws RemoteException {
            return currentStatus.getRangeStatus();
        }

        @Override
        public float getSpeedLevel() throws RemoteException {
            return currentStatus.getspeedLevel();
        }

        @Override 
        public boolean getSpeedWarning() throws RemoteException {
            return currentStatus.getSpeedWarning();
        }

        @Override
        public String getGearStatus() throws RemoteException {
            return currentStatus.getGearStatus();
        }
    };

    private final CarPropertyManager.CarPropertyEventCallback propertyCallback = 
    new CarPropertyManager.CarPropertyEventCallback() {
        @Override
        public void onChangeEvent(CarPropertyValue value) {
            try {
                if (value != null) {
                    Log.d(TAG, String.format("Property changed - ID: %d, Value: %s",
                        value.getPropertyId(), value.getValue()));
                    propertyHandler.handlePropertyChange(value);
                    broadcastStatus();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling property change", e);
            }
        }

        @Override
        public void onErrorEvent(int propertyId, int areaId) {
            Log.e(TAG, String.format("Property error - ID: %d, Area: 0x%x", 
                propertyId, areaId));
        }

        @Override
        public void onErrorEvent(int propertyId, int areaId, int errorCode) {
            Log.e(TAG, String.format("Property error - ID: %d, Area: 0x%x, Code: %d",
                propertyId, areaId, errorCode));
            
            // Simplified error handling without undefined constants
            if (errorCode != 0) {
                Log.e(TAG, "Operation failed with error code: " + errorCode);
            }
        }
    };

    @Override
    public void onCreate() {
        if (instance != null) {
            Log.d(TAG, "Service instance already exists, stopping duplicate");
            stopSelf();
            return;
        }

        synchronized (EvSafeService.class) {
            if (instance == null) {
                super.onCreate();
                Log.d(TAG, "Service onCreate");
                try {
                    initializeComponents();
                    broadcastsEnabled = true; // Enable broadcasts by default
                    startEvSafeService();
                    instance = this;
                } catch (Exception e) {
                    Log.e(TAG, "Failed to initialize service", e);
                    stopSelf();
                }
            } else {
                Log.d(TAG, "Service instance already exists (after sync), stopping duplicate");
                stopSelf();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        cleanup();
        super.onDestroy();
    }

    private void initializeComponents() {
        currentStatus = new EvSafeServiceStatus();
        notificationHelper = new EvSafeServiceNotificationHelper(this, currentStatus);

        try {
            initializeCarComponents();
            Log.d(TAG, "Components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize car components", e);
            throw new IllegalStateException("Car initialization failed", e);
        }
    }

    private void initializeCarComponents() {
        car = Car.createCar(this);
        Objects.requireNonNull(car, "Failed to create Car instance");

        propertyManager = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);
        Objects.requireNonNull(propertyManager, "Failed to get CarPropertyManager");

        propertyHandler = new EvSafeServiceVehiclePropertyHandler(
            propertyManager,
            currentStatus,
            notificationHelper,
            () -> broadcastStatus()
        );
    }

    private synchronized void startEvSafeService() {
        if (isServiceRunning) {
            Log.d(TAG, "Service already running");
            return;
        }
        
        try {
            isServiceRunning = true;
            propertyHandler.registerListeners();
            Log.d(TAG, "Service started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start service", e);
            stopSelf();
        }
    }

    private synchronized void stopEvSafeService() {
        if (!isServiceRunning) return;
        
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

    private void cleanup() {
        try {
            stopEvSafeService();
            disconnectCar();
            clearReferences();
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }
    }

    private void disconnectCar() {
        if (car != null) {
            car.disconnect();
            car = null;
        }
    }

    private void clearReferences() {
        propertyManager = null;
        propertyHandler = null;
        notificationHelper = null;
        currentStatus = null;
        instance = null;
    }
}