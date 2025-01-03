package com.android.car.evsafeservice;

// Android core
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.UserHandle;
import android.os.RemoteException;
import android.util.Log;

// Android car
import android.car.Car;
// import android.car.VehicleGear;
// import android.car.VehiclePropertyIds;
import android.car.hardware.property.CarPropertyManager;
// import android.car.hardware.property.CarPropertyEvent;
import android.car.hardware.CarPropertyValue;

// Android car service
import android.car.evsafeservice.EvSafeServiceApi;
import android.car.evsafeservice.EvSafeServiceStatusListener;
import android.car.evsafeservice.EvSafeServiceStatusData;

// Java utils
import java.util.Objects;

/**
 * Service that monitors electric vehicle status and broadcasts updates.
 * Implements singleton pattern to ensure only one instance runs.
 */
public class EvSafeService extends Service {
    private static final String TAG = "EvSafeService";
    private static final String ACTION_STATUS_UPDATE = "com.android.car.evsafeservice.ACTION_STATUS_UPDATE";
    private static final String PERMISSION_RECEIVE_STATUS = "com.android.car.evsafeservice.permission.RECEIVE_STATUS_UPDATE";

    private static final class Extras {
        static final String BATTERY_STATUS = "batteryStatus";
        static final String BATTERY_LEVEL = "batteryLevel";
        static final String RANGE_STATUS = "rangeStatus";
        static final String SPEED_LEVEL = "speedLevel";
        static final String GEAR_STATUS = "gearStatus";
        static final String SPEED_WARNING = "speedWarning";
    }

    // Singleton with double-checked locking
    private static volatile EvSafeService instance;
    private static final Object LOCK = new Object();

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

    @Override
    public void onCreate() {
        synchronized (LOCK) {
            if (instance != null) {
                Log.d(TAG, "Service instance already exists 0103");
                stopSelf();
                return;
            }
            super.onCreate();
            try {
                initializeService();
                instance = this;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize service", e);
                cleanup();
                stopSelf();
            }
        }
    }

    private void initializeService() {
        Log.d(TAG, "Initializing service");
        currentStatus = new EvSafeServiceStatus();
        notificationHelper = new EvSafeServiceNotificationHelper(this, currentStatus);
        initializeCarComponents();
        broadcastsEnabled = true;
    }

    private void initializeCarComponents() {
        try {
            car = Car.createCar(this);
            Objects.requireNonNull(car, "Failed to create Car instance");

            propertyManager = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);
            Objects.requireNonNull(propertyManager, "Failed to get CarPropertyManager");

            propertyHandler = new EvSafeServiceVehiclePropertyHandler(
                propertyManager,
                currentStatus,
                notificationHelper,
                this::broadcastStatus
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize car components", e);
        }
    }

    // Broadcast status updates
    private void broadcastStatus() {
        if (!broadcastsEnabled) {
            Log.d(TAG, "Broadcasts disabled, skipping update");
            return;
        }
    
        try {
            Intent intent = new Intent(ACTION_STATUS_UPDATE);
            
            intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        
            // Add values using constants
            intent.putExtra(Extras.BATTERY_STATUS, currentStatus.getBatteryStatus())
                .putExtra(Extras.BATTERY_LEVEL, currentStatus.getBatteryLevel())
                .putExtra(Extras.RANGE_STATUS, currentStatus.getRangeStatus())
                .putExtra(Extras.SPEED_LEVEL, currentStatus.getSpeedLevel())
                .putExtra(Extras.GEAR_STATUS, currentStatus.getGearStatus())
                .putExtra(Extras.SPEED_WARNING, currentStatus.getSpeedWarning());
            
            Log.d(TAG, String.format("Broadcasting status - Battery: %.1f%%, Speed: %.1f km/h, Gear: %s", 
                currentStatus.getBatteryStatus(),
                currentStatus.getSpeedLevel(),
                currentStatus.getGearStatus()));

            sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_RECEIVE_STATUS);
            
            notifyStatusListener();
        } catch (Exception e) {
            Log.e(TAG, "Failed to broadcast status", e);
        }
    }

    private void notifyStatusListener() {
        if (statusListener != null) {
            try {
                statusListener.onStatusChanged(convertToStatusData());
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to notify status listener", e);
            }
        }
    }

    private EvSafeServiceStatusData convertToStatusData() {
        EvSafeServiceStatusData data = new EvSafeServiceStatusData();
        data.batteryStatus = currentStatus.getBatteryStatus();
        data.batteryLevel = currentStatus.getBatteryLevel();
        data.rangeStatus = currentStatus.getRangeStatus();
        data.speedLevel = currentStatus.getSpeedLevel();
        data.gearStatus = currentStatus.getGearStatus();
        data.speedWarning = currentStatus.getSpeedWarning();
        return data;
    }


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
            handlePropertyError(propertyId, areaId, 0);
        }

        @Override
        public void onErrorEvent(int propertyId, int areaId, int errorCode) {
            handlePropertyError(propertyId, areaId, errorCode);
        }
    };

    private void handlePropertyError(int propertyId, int areaId, int errorCode) {
        Log.e(TAG, String.format("Property error - ID: %d, Area: 0x%x, Code: %d",
            propertyId, areaId, errorCode));
        if (statusListener != null) {
            try {
                statusListener.onError(errorCode, "Property error occurred");
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to notify error to listener", e);
            }
        }
    }


    // AIDL interface implementation
    private final EvSafeServiceApi.Stub binder = new EvSafeServiceApi.Stub() {
        @Override
        public void startService() throws RemoteException {
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
            return currentStatus.getSpeedLevel();
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
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");
        startEvSafeService();
        return START_STICKY;
    }
    
    private synchronized void startEvSafeService() {
        if (isServiceRunning) {
            Log.d(TAG, "Service already running");
            return;
        }
        
        try {
            propertyHandler.registerListeners();
            isServiceRunning = true;
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

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        cleanup();
        super.onDestroy();
    }

    private void cleanup() {
        stopEvSafeService();
        if (car != null) {
            car.disconnect();
            car = null;
        }
        propertyManager = null;
        propertyHandler = null;
        notificationHelper = null;
        currentStatus = null;
        synchronized (LOCK) {
            instance = null;
        }
    }
}