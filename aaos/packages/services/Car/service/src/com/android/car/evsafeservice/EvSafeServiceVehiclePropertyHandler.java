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
    public static final String TAG = "EvSafeServiceVehiclePropertyHandler";
    
    private final CarPropertyManager propertyManager;
    private final EvSafeServiceStatus currentStatus;
    private final EvSafeServiceNotificationHelper notificationHelper;
    private boolean isLowRangeWarningShown;
    private boolean isLowBatteryWarningShown;
    private EvSafeServiceStatus lastStatus;

    public interface BroadcastCallback {
        void onPropertyChanged();
    }

    private final BroadcastCallback broadcastCallback;

    public EvSafeServiceVehiclePropertyHandler(
            CarPropertyManager propertyManager,
            EvSafeServiceStatus currentStatus,
            EvSafeServiceNotificationHelper notificationHelper,
            BroadcastCallback broadcastCallback) {
        this.propertyManager = Objects.requireNonNull(propertyManager, "PropertyManager cannot be null");
        this.currentStatus = Objects.requireNonNull(currentStatus, "EvSStatus cannot be null");
        this.notificationHelper = Objects.requireNonNull(notificationHelper, "NotificationHelper cannot be null");
        this.broadcastCallback = Objects.requireNonNull(broadcastCallback, "BroadcastCallback cannot be null");
        this.lastStatus = new EvSafeServiceStatus(currentStatus);
    }


    /**
     * Registers callbacks for vehicle property changes
     */
    public void registerListeners() {
        try {
            // 각 속성의 changeMode 확인
            CarPropertyConfig<?> speedConfig = propertyManager.getCarPropertyConfig(VehiclePropertyIds.PERF_VEHICLE_SPEED);
            CarPropertyConfig<?> batteryConfig = propertyManager.getCarPropertyConfig(VehiclePropertyIds.EV_BATTERY_LEVEL);
            CarPropertyConfig<?> gearConfig = propertyManager.getCarPropertyConfig(VehiclePropertyIds.GEAR_SELECTION);
            Log.d(TAG, String.format("Speed change mode: %s (mode:%d), min rate: %.1f, max rate: %.1f", 
                getChangeModeString(speedConfig.getChangeMode()),
                speedConfig.getChangeMode(),
                speedConfig.getMinSampleRate(),
                speedConfig.getMaxSampleRate()));
                
            Log.d(TAG, String.format("Battery change mode: %s (mode:%d), min rate: %.1f, max rate: %.1f",
                getChangeModeString(batteryConfig.getChangeMode()),
                batteryConfig.getChangeMode(),
                batteryConfig.getMinSampleRate(),
                batteryConfig.getMaxSampleRate()));
    
            Log.d(TAG, String.format("Gear change mode: %s (mode:%d), min rate: %.1f, max rate: %.1f",
                getChangeModeString(gearConfig.getChangeMode()),
                gearConfig.getChangeMode(),
                gearConfig.getMinSampleRate(),
                gearConfig.getMaxSampleRate()));
    
            propertyManager.registerCallback(propertyEventCallback,
                VehiclePropertyIds.PERF_VEHICLE_SPEED, 
                EvSafeServiceConstants.SensorRate.SPEED_SAMPLE_RATE);
                
            propertyManager.registerCallback(propertyEventCallback,
                VehiclePropertyIds.EV_BATTERY_LEVEL, 
                EvSafeServiceConstants.SensorRate.BATTERY_SAMPLE_RATE);
                
            propertyManager.registerCallback(propertyEventCallback,
                VehiclePropertyIds.GEAR_SELECTION,
                CarPropertyManager.SENSOR_RATE_ONCHANGE);
                
        } catch (Exception e) {
            Log.e(TAG, "Failed to register property callbacks", e);
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

    /**
     * Unregisters all property callbacks
     */
    public void unregisterListeners() {
        try {
            propertyManager.unregisterCallback(propertyEventCallback);
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister property callbacks", e);
        }
    }

    private final CarPropertyManager.CarPropertyEventCallback propertyEventCallback = 
        new CarPropertyManager.CarPropertyEventCallback() {
            @Override
            public void onChangeEvent(CarPropertyValue value) {
                if (value != null) {
                    handlePropertyChange(value);
                }
            }

            @Override
            public void onErrorEvent(int propertyId, int areaId) {
                Log.e(TAG, String.format("Error event for property: %d, areaId: %d", 
                    propertyId, areaId));
            }
        };

    public void handlePropertyChange(CarPropertyValue<?> value) {
        if (value == null) {
            Log.w(TAG, "Received null property value");
            return;
        }
    
        try {
            int propertyId = value.getPropertyId();
            Object propertyValue = value.getValue();
            boolean valueChanged = false;
    
            switch (propertyId) {
                case VehiclePropertyIds.GEAR_SELECTION:
                    if (propertyValue instanceof Integer) {
                        String gearStatus = convertGearToString((Integer) propertyValue);
                        Log.d(TAG, "Received gear change: " + gearStatus);
                        currentStatus.setGearStatus(gearStatus);
                        valueChanged = true;
                    }
                    break;
    
                case VehiclePropertyIds.PERF_VEHICLE_SPEED:
                    if (propertyValue instanceof Float) {
                        Float newSpeed = convertSpeedLevel((Float) propertyValue);
                        float currentSpeed = currentStatus.getspeedLevel();
                        if (!Float.valueOf(currentSpeed).equals(newSpeed)) {
                            updateSpeedStatus(newSpeed);
                            valueChanged = true;
                        }
                    }
                    break;
    
                case VehiclePropertyIds.EV_BATTERY_LEVEL:
                    if (propertyValue instanceof Float) {
                        Float newBattery = (Float) propertyValue;
                        float currentBattery = currentStatus.getBatteryLevel();
                        if (!Float.valueOf(currentBattery).equals(newBattery)) {
                            updateBatteryLevel(newBattery);
                            valueChanged = true;
                        }
                    }
                    break;
    
                default:
                    Log.w(TAG, String.format("Unexpected property ID: %d", propertyId));
                    return;
            }
    
            if (valueChanged) {
                broadcastCallback.onPropertyChanged();
            }
    
        } catch (Exception e) {
            Log.e(TAG, "Error handling property change", e);
        }
    }

    private void updateSpeedStatus(Float speedValue) {
        if (speedValue != null) {
            try {
                currentStatus.setspeedLevel(speedValue);
                // Speed Warning Check
                if (speedValue > EvSafeServiceConstants.Speed.SPEED_WARNING_THRESHOLD 
                    && !currentStatus.getSpeedWarning()) {
                    currentStatus.setSpeedWarning(true);
                    Log.d(TAG, "Speed warning triggered: " + speedValue);
                    notificationHelper.showWarningNotification(
                        "High Speed Warning",
                        String.format("Speed exceeds %.1f km/h", speedValue));
                } else if (speedValue <= EvSafeServiceConstants.Speed.SPEED_WARNING_THRESHOLD) {
                    currentStatus.setSpeedWarning(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating speed status", e);
            }
        }
    }

    private void updateBatteryLevel(Float batteryValue) {
        if (batteryValue != null) {
            try {
                currentStatus.setBatteryLevel(batteryValue);
                float batteryPercentage = calculateBatteryPercentage(batteryValue);
                currentStatus.setBatteryStatus(batteryPercentage);
                updateRangeEstimate(batteryPercentage);
            } catch (Exception e) {
                Log.e(TAG, "Error updating battery status", e);
            }
        }
    }


    private boolean checkBatteryWarning() {
        float batteryLevel = currentStatus.getBatteryStatus();
        if (batteryLevel < EvSafeServiceConstants.VehicleStatus.LOW_BATTERY_THRESHOLD_PERCENT 
            && !isLowBatteryWarningShown) {
            isLowBatteryWarningShown = true;
            Log.d(TAG, "Low battery warning triggered: " + batteryLevel);
            notificationHelper.showWarningNotification(
                "Low Battery Warning",
                String.format("Battery level critical: %.1f%%", batteryLevel));
            return true;
        } else if (batteryLevel >= EvSafeServiceConstants.VehicleStatus.LOW_BATTERY_THRESHOLD_PERCENT) {
            isLowBatteryWarningShown = false;
        }
        return false;
    }

    private float convertSpeedLevel(Float speedValue) {
        if (speedValue == null) {
            throw new IllegalArgumentException("Speed value cannot be null");
        }
        return speedValue * EvSafeServiceConstants.Speed.SPEED_TO_KMH_MULTIPLIER;
    }

    private String convertGearToString(int gearValue) {
        switch (gearValue) {
            case VehicleGear.GEAR_PARK: return "P";
            case VehicleGear.GEAR_REVERSE: return "R";
            case VehicleGear.GEAR_NEUTRAL: return "N";
            case VehicleGear.GEAR_DRIVE: return "D";
            default: return EvSafeServiceConstants.Defaults.GEAR_STATUS;
        }
    }

    private float getBatteryCapacity() {
        try {
            return propertyManager.getFloatProperty(
                VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting battery capacity", e);
            return EvSafeServiceConstants.Defaults.STATUS_VALUE;
        }
    }

    private float calculateBatteryPercentage(float rawValue) {
        float capacity = getBatteryCapacity();
        if (capacity <= 0) {
            Log.w(TAG, "Invalid battery capacity");
            return EvSafeServiceConstants.Defaults.STATUS_VALUE;
        }
        return (rawValue / capacity) * EvSafeServiceConstants.Battery.PERCENTAGE_MULTIPLIER;
    }

    private void updateRangeEstimate(float batteryPercentage) {
        float speedFactor = calculateSpeedFactor(currentStatus.getspeedLevel());
        float estimatedRange = (batteryPercentage / EvSafeServiceConstants.Battery.PERCENTAGE_MULTIPLIER) 
            * EvSafeServiceConstants.VehicleStatus.MAX_RANGE_KM 
            * speedFactor;
        currentStatus.setRangeStatus(estimatedRange);
    }

    private float calculateSpeedFactor(float speed) {
        return Math.max(
            EvSafeServiceConstants.Speed.MIN_SPEED_FACTOR,
            1.0f - (speed * EvSafeServiceConstants.Speed.SPEED_FACTOR_MULTIPLIER)
        );
    }
}