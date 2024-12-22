package com.android.car.evsafeservice;

import android.car.hardware.property.CarPropertyManager;
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

    /**
     * Creates a new VehiclePropertyHandler
     * @throws NullPointerException if any parameter is null
     */
    public EvSafeServiceVehiclePropertyHandler(CarPropertyManager propertyManager, 
                                EvSafeServiceStatus currentStatus,
                                EvSafeServiceNotificationHelper notificationHelper) {
        this.propertyManager = Objects.requireNonNull(propertyManager, "PropertyManager cannot be null");
        this.currentStatus = Objects.requireNonNull(currentStatus, "EvSStatus cannot be null");
        this.notificationHelper = Objects.requireNonNull(notificationHelper, "NotificationHelper cannot be null");
    }

    /**
     * Registers callbacks for vehicle property changes
     */
    public void registerListeners() {
        try {
            propertyManager.registerCallback(propertyEventCallback, 
                VehiclePropertyIds.GEAR_SELECTION, 
                CarPropertyManager.SENSOR_RATE_ONCHANGE);
            propertyManager.registerCallback(propertyEventCallback, 
                VehiclePropertyIds.PERF_VEHICLE_SPEED, 
                CarPropertyManager.SENSOR_RATE_ONCHANGE);
            propertyManager.registerCallback(propertyEventCallback, 
                VehiclePropertyIds.EV_BATTERY_LEVEL, 
                CarPropertyManager.SENSOR_RATE_ONCHANGE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register property callbacks", e);
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


    private void handlePropertyChange(CarPropertyValue<?> value) {
        try {
            switch (value.getPropertyId()) {
                case VehiclePropertyIds.GEAR_SELECTION:
                    updateGearStatus((Integer) value.getValue());
                    break;
                case VehiclePropertyIds.PERF_VEHICLE_SPEED:
                    updateSpeedStatus((Float) value.getValue());
                    break;
                case VehiclePropertyIds.EV_BATTERY_LEVEL:
                    updateBatteryStatus((Float) value.getValue());
                    break;
                default:
                    Log.w(TAG, "Unexpected property ID: " + value.getPropertyId());
            }
            checkWarningConditions();
            notificationHelper.updateServiceNotification();
        } catch (Exception e) {
            Log.e(TAG, "Error handling property change", e);
        }
    }

    private void updateGearStatus(Integer gearValue) {
        if (gearValue != null) {
            try {
                currentStatus.setGearStatus(convertGearToString(gearValue));
            } catch (Exception e) {
                Log.e(TAG, "Error updating gear status", e);
            }
        }
    }

    private void updateSpeedStatus(Float speedValue) {
        if (speedValue != null) {
            try {
                currentStatus.setSpeedStatus(
                    speedValue * EvSafeServiceConstants.Speed.SPEED_TO_KMH_MULTIPLIER);
            } catch (Exception e) {
                Log.e(TAG, "Error updating speed status", e);
            }
        }
    }

    private void updateBatteryStatus(Float batteryValue) {
        if (batteryValue != null) {
            try {
                float batteryPercentage = calculateBatteryPercentage(batteryValue);
                currentStatus.setBatteryStatus(batteryPercentage);
                updateRangeEstimate(batteryPercentage);
            } catch (Exception e) {
                Log.e(TAG, "Error updating battery status", e);
            }
        }
    }

    private void checkWarningConditions() {
        try {
            checkBatteryWarning();
            checkRangeWarning();
        } catch (Exception e) {
            Log.e(TAG, "Error checking warning conditions", e);
        }
    }

    private void checkBatteryWarning() {
        float batteryLevel = currentStatus.getBatteryStatus();
        if (batteryLevel < EvSafeServiceConstants.VehicleStatus.LOW_BATTERY_THRESHOLD_PERCENT 
            && !isLowBatteryWarningShown) {
            isLowBatteryWarningShown = true;
            notificationHelper.showWarningNotification(
                "Low Battery Warning",
                String.format("Battery level critical: %.1f%%", batteryLevel));
        } else if (batteryLevel >= EvSafeServiceConstants.VehicleStatus.LOW_BATTERY_THRESHOLD_PERCENT) {
            isLowBatteryWarningShown = false;
        }
    }

    private void checkRangeWarning() {
        float range = currentStatus.getRangeStatus();
        if (range < EvSafeServiceConstants.VehicleStatus.LOW_RANGE_THRESHOLD_KM 
            && !isLowRangeWarningShown) {
            isLowRangeWarningShown = true;
            notificationHelper.showWarningNotification(
                "Low Range Warning",
                String.format("Estimated range: %.1f km", range));
        } else if (range >= EvSafeServiceConstants.VehicleStatus.LOW_RANGE_THRESHOLD_KM) {
            isLowRangeWarningShown = false;
        }
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

    private float calculateBatteryPercentage(float rawValue) {
        return (rawValue / getBatteryCapacity()) * EvSafeServiceConstants.Battery.PERCENTAGE_MULTIPLIER;
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

    private void updateRangeEstimate(float batteryPercentage) {
        float speedFactor = calculateSpeedFactor(currentStatus.getSpeedStatus());
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