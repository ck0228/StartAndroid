package com.android.car.evsafeservice;

import android.car.hardware.property.CarPropertyManager;
import android.car.hardware.CarPropertyConfig;
import android.car.hardware.CarPropertyValue;
import android.car.VehiclePropertyIds;
import android.car.VehicleGear;

import android.util.Log;
import java.util.Objects;

/**
 * Handles vehicle property updates and manages property listeners
 */
public class EvSafeServiceVehiclePropertyHandler {
    private static final String TAG = "EvSafeServiceVehiclePropertyHandler";
    
    private final CarPropertyManager propertyManager;
    private final EvSafeServiceStatus currentStatus;
    private final EvSafeServiceNotificationHelper notificationHelper;
    private final BroadcastCallback broadcastCallback;
    private final CarPropertyManager.CarPropertyEventCallback propertyCallback;

    // State
    private boolean isSpeedWarningShown;
    private boolean isBatteryWarningShown;

    public interface BroadcastCallback {
        void onPropertyChanged();
    }

    public EvSafeServiceVehiclePropertyHandler(
            CarPropertyManager propertyManager,
            EvSafeServiceStatus currentStatus,
            EvSafeServiceNotificationHelper notificationHelper,
            BroadcastCallback broadcastCallback) {
        this.propertyManager = Objects.requireNonNull(propertyManager, "PropertyManager cannot be null");
        this.currentStatus = Objects.requireNonNull(currentStatus, "Status cannot be null");
        this.notificationHelper = Objects.requireNonNull(notificationHelper, "NotificationHelper cannot be null");
        this.broadcastCallback = Objects.requireNonNull(broadcastCallback, "BroadcastCallback cannot be null");
        
        this.propertyCallback = new CarPropertyManager.CarPropertyEventCallback() {
            @Override
            public void onChangeEvent(CarPropertyValue value) {
                handlePropertyChange(value);
            }

            @Override
            public void onErrorEvent(int propertyId, int areaId) {
                Log.e(TAG, "Property error - ID: " + propertyId + ", Area: " + areaId);
            }
        };
    }

    /**
     * Registers callbacks for vehicle property changes
     * SENSOR_RATE_NORMAL: 1hz sampling rate
     */
    public void registerListeners() {
        try {
            checkPropertyConfigs();
            propertyManager.registerCallback(propertyCallback, VehiclePropertyIds.PERF_VEHICLE_SPEED, 
                CarPropertyManager.SENSOR_RATE_NORMAL);
            propertyManager.registerCallback(propertyCallback, VehiclePropertyIds.GEAR_SELECTION, 
                CarPropertyManager.SENSOR_RATE_NORMAL);
            propertyManager.registerCallback(propertyCallback, VehiclePropertyIds.EV_BATTERY_LEVEL, 
                CarPropertyManager.SENSOR_RATE_NORMAL);
            Log.d(TAG, "Property listeners registered");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register property callbacks", e);
        }
    }

    /**
     * Unregisters all property callbacks
     */
    public void unregisterListeners() {
        try {
            propertyManager.unregisterCallback(propertyCallback);
            Log.d(TAG, "Property listeners unregistered");
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister property listeners", e);
        }
    }

    public void handlePropertyChange(CarPropertyValue<?> value) {
        if (value == null) return;

        try {
            switch (value.getPropertyId()) {
                case VehiclePropertyIds.PERF_VEHICLE_SPEED:
                    updateSpeedStatus((Float) value.getValue());
                    break;
                case VehiclePropertyIds.GEAR_SELECTION:
                    updateGearStatus((Integer) value.getValue());
                    break;
                case VehiclePropertyIds.EV_BATTERY_LEVEL:
                    updateBatteryLevel((Float) value.getValue());
                    break;
                default:
                    Log.w(TAG, "Unhandled property ID: " + value.getPropertyId());
                    break;
            }
            broadcastCallback.onPropertyChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error handling property change", e);
        }
    }

    private void updateSpeedStatus(Float speedValue) {
        if (speedValue == null) return;
        
        float speedKmh = convertSpeedToKmh(speedValue);
        currentStatus.setSpeedLevel(speedKmh);
        
        // Update speed warning
        boolean isOverSpeed = speedKmh > EvSafeServiceConstants.Speed.SPEED_WARNING_THRESHOLD;
        if (isOverSpeed != isSpeedWarningShown) {
            isSpeedWarningShown = isOverSpeed;
            currentStatus.setSpeedWarning(isOverSpeed);
            
            if (isOverSpeed) {
                notificationHelper.showWarningNotification(
                    "Speed Warning",
                    String.format("Vehicle speed exceeds %.0f km/h", speedKmh)
                );
            }
        }
    }

    private void updateGearStatus(Integer gearValue) {
        if (gearValue == null) return;
        currentStatus.setGearStatus(convertGearToString(gearValue));
    }

    private void updateBatteryLevel(Float batteryValue) {
        if (batteryValue == null) return;
        
        float batteryPercentage = calculateBatteryPercentage(batteryValue);
        currentStatus.setBatteryLevel(batteryValue);
        currentStatus.setBatteryStatus(batteryPercentage);
        updateRangeEstimate(batteryPercentage);
        
        // Check battery warnings
        checkBatteryWarnings(batteryPercentage);
    }

    private float convertSpeedToKmh(float rawSpeed) {
        return rawSpeed * EvSafeServiceConstants.Speed.SPEED_TO_KMH_MULTIPLIER;
    }

    private String convertGearToString(int gearValue) {
        switch (gearValue) {
            case VehicleGear.GEAR_PARK: return "P";
            case VehicleGear.GEAR_NEUTRAL: return "N";
            case VehicleGear.GEAR_REVERSE: return "R";
            case VehicleGear.GEAR_DRIVE: return "D";
            default: return "Unknown";
        }
    }

    private float calculateBatteryPercentage(float batteryLevel) {
        try {
            float batteryCapacity = propertyManager.getFloatProperty(
                VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY, 0);
            
            if (batteryCapacity <= 0) {
                Log.w(TAG, "Invalid battery capacity: " + batteryCapacity);
                return 0.0f;
            }

            return Math.min(100.0f, Math.max(0.0f, 
                (batteryLevel / batteryCapacity) * 100.0f));
        } catch (Exception e) {
            Log.e(TAG, "Error calculating battery percentage", e);
            return 0.0f;
        }
    }

    private void updateRangeEstimate(float batteryPercentage) {
        float maxRange = EvSafeServiceConstants.VehicleStatus.MAX_RANGE_KM;
        float estimatedRange = (batteryPercentage / 100.0f) * maxRange;
        currentStatus.setRangeStatus(estimatedRange);
    }

    private void checkBatteryWarnings(float batteryPercentage) {
        boolean isLowBattery = batteryPercentage <= EvSafeServiceConstants.VehicleStatus.LOW_BATTERY_THRESHOLD_PERCENT;
        if (isLowBattery != isBatteryWarningShown) {
            isBatteryWarningShown = isLowBattery;
            if (isLowBattery) {
                notificationHelper.showWarningNotification(
                    "Low Battery Warning",
                    String.format("Battery level is %.0f%%", batteryPercentage)
                );
            }
        }
    }

    private void checkPropertyConfigs() {
        try {
            logPropertyConfig("Speed", 
                propertyManager.getCarPropertyConfig(VehiclePropertyIds.PERF_VEHICLE_SPEED));
            logPropertyConfig("Battery", 
                propertyManager.getCarPropertyConfig(VehiclePropertyIds.EV_BATTERY_LEVEL));
            logPropertyConfig("Gear", 
                propertyManager.getCarPropertyConfig(VehiclePropertyIds.GEAR_SELECTION));
        } catch (Exception e) {
            Log.e(TAG, "Failed to check property configs", e);
        }
    }

    private void logPropertyConfig(String propertyName, CarPropertyConfig<?> config) {
        if (config != null) {
            Log.d(TAG, String.format("%s change mode: %s (mode:%d), min rate: %.1f, max rate: %.1f",
                propertyName,
                getChangeModeString(config.getChangeMode()),
                config.getChangeMode(),
                config.getMinSampleRate(),
                config.getMaxSampleRate()));
        }
    }

    private String getChangeModeString(int mode) {
        switch (mode) {
            case CarPropertyConfig.VEHICLE_PROPERTY_CHANGE_MODE_STATIC:
                return "STATIC";
            case CarPropertyConfig.VEHICLE_PROPERTY_CHANGE_MODE_ONCHANGE:
                return "ONCHANGE";
            case CarPropertyConfig.VEHICLE_PROPERTY_CHANGE_MODE_CONTINUOUS:
                return "CONTINUOUS";
            default:
                return "UNKNOWN";
        }
    }
}