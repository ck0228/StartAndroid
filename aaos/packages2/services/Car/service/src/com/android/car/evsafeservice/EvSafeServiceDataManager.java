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
public class EvSafeServiceDataManager {
    public static final String TAG = "EvSafeServiceDataManager";
    
    private final CarPropertyManager carPropertyManager;
    private final EvSafeServiceData evSafeServiceData;

    public interface EvSafeServiceDataUpdateListener {
        void onEvSafeServiceDataUpdated(EvSafeServiceData evSafeServiceData);
    }

    private final EvSafeServiceDataUpdateListener evSafeServiceDataUpdateListener;


    public EvSafeServiceDataManager(CarPropertyManager carPropertyManager, EvSafeServiceData evSafeServiceData, EvSafeServiceDataUpdateListener evSafeServiceDataUpdateListener) {
        this.carPropertyManager = Objects.requireNonNull(carPropertyManager, "CarPropertyManager cannot be null");
        this.evSafeServiceData = Objects.requireNonNull(evSafeServiceData, "EvSafeServiceData cannot be null");
        this.evSafeServiceDataUpdateListener = Objects.requireNonNull(evSafeServiceDataUpdateListener, "EvSafeServiceDataUpdateListener cannot be null");
    }


    public void registerListeners() {
        try {
            // 각 속성의 changeMode 확인
            CarPropertyConfig<?> speedConfig = carPropertyManager.getCarPropertyConfig(VehiclePropertyIds.PERF_VEHICLE_SPEED);
            CarPropertyConfig<?> batteryConfig = carPropertyManager.getCarPropertyConfig(VehiclePropertyIds.EV_BATTERY_LEVEL);
            CarPropertyConfig<?> gearConfig = carPropertyManager.getCarPropertyConfig(VehiclePropertyIds.GEAR_SELECTION);
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
    
            carPropertyManager.registerCallback(carPropertyEventCallback,
                VehiclePropertyIds.PERF_VEHICLE_SPEED, 
                EvSafeServiceConfigs.SENSOR_RATE);
                
            carPropertyManager.registerCallback(carPropertyEventCallback,
                VehiclePropertyIds.EV_BATTERY_LEVEL, 
                EvSafeServiceConfigs.SENSOR_RATE);
                
            carPropertyManager.registerCallback(carPropertyEventCallback,
                VehiclePropertyIds.GEAR_SELECTION,
                CarPropertyManager.SENSOR_RATE_ONCHANGE);
                
        } catch (Exception e) {
            Log.e(TAG, "Failed to register property callbacks", e);
        }
    }

    public void unregisterListeners() {
        try {
            carPropertyManager.unregisterCallback(VehiclePropertyIds.PERF_VEHICLE_SPEED, carPropertyEventCallback);
            carPropertyManager.unregisterCallback(VehiclePropertyIds.EV_BATTERY_LEVEL, carPropertyEventCallback);
            carPropertyManager.unregisterCallback(VehiclePropertyIds.GEAR_SELECTION, carPropertyEventCallback);
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister property callbacks", e);
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

    private final CarPropertyManager.CarPropertyEventCallback carPropertyEventCallback = new CarPropertyManager.CarPropertyEventCallback() {
        @Override
        public void onChangeEvent(CarPropertyValue<?> value) {
            if (value != null) {
                handlePropertyChange(value);
            }
        }
    
        @Override
        public void onErrorEvent(int propertyId, int zone) {
            Log.e(TAG, "Error event received for property: " + propertyId + ", zone: " + zone);
        }
    };

    private void handlePropertyChange(CarPropertyValue<?> value) {
        EvSafeServiceData oldData = new EvSafeServiceData(evSafeServiceData); // 현재 데이터를 복사하여 저장

        int propertyId = value.getPropertyId();
        switch (propertyId) {
            case VehiclePropertyIds.PERF_VEHICLE_SPEED:
                Float rawSpeed = (Float) value.getValue();
                float speedKmh = convertSpeedLevel(rawSpeed);
                evSafeServiceData.setSpeed(speedKmh);
                updateSpeedWarning(speedKmh);
                break;
            case VehiclePropertyIds.EV_BATTERY_LEVEL:
                Float rawBatteryLevel = (Float) value.getValue();
                updateBatteryStatus(rawBatteryLevel);
                break;
            case VehiclePropertyIds.GEAR_SELECTION:
                updateGearStatus((int) value.getValue());
                break;
            default:
                Log.d(TAG, "Unhandled property change: " + propertyId);
                break;
        }
            // 데이터가 변경된 경우에만 업데이트 알림
        if (!evSafeServiceData.equals(oldData)) {
            evSafeServiceDataUpdateListener.onEvSafeServiceDataUpdated(evSafeServiceData);
        }
    }
    
    private void updateSpeedWarning(float speed) {
        boolean isWarning = speed > EvSafeServiceConfigs.Thresholds.SPEED_WARNING;
        evSafeServiceData.setSpeedWarning(isWarning);
    }
    
    private void updateGearStatus(int gearValue) {
        String gearStatus = convertGearToString(gearValue);
        evSafeServiceData.setGearStatus(gearStatus);
    }
    
    private void updateBatteryStatus(Float rawBatteryLevel) {
        if (rawBatteryLevel == null) {
            Log.w(TAG, "Raw battery level is null");
            return;
        }

        evSafeServiceData.setBatteryLevel(rawBatteryLevel);

        float batteryPercentage = calculateBatteryPercentage(rawBatteryLevel);
        evSafeServiceData.setBatteryStatus(batteryPercentage);
    
        float estimatedRange = calculateEstimatedRange(batteryPercentage);
        evSafeServiceData.setRange(estimatedRange);
    }

    private String convertGearToString(int gearValue) {
        switch (gearValue) {
            case VehicleGear.GEAR_PARK: return "P";
            case VehicleGear.GEAR_REVERSE: return "R";
            case VehicleGear.GEAR_NEUTRAL: return "N";
            case VehicleGear.GEAR_DRIVE: return "D";
            default: return EvSafeServiceConfigs.Defaults.GEAR_STATUS;
        }
    }


    private float convertSpeedLevel(Float speedValue) {
        if (speedValue == null) {
            throw new IllegalArgumentException("Speed value cannot be null");
        }
        return speedValue * EvSafeServiceConfigs.TO_KMH_MULTIPLIER;
    }
    
    private float getBatteryCapacity() {
        try {
            return carPropertyManager.getFloatProperty(
                VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting battery capacity", e);
            return EvSafeServiceConfigs.Defaults.STATUS_VALUE;
        }
    }

    private float calculateBatteryPercentage(float rawValue) {
        float capacity = getBatteryCapacity();
        if (capacity <= 0) {
            Log.w(TAG, "Invalid battery capacity");
            return EvSafeServiceConfigs.Defaults.STATUS_VALUE;
        }
        return (rawValue / capacity) * 100;
    }

    private float calculateEstimatedRange(float batteryPercentage) {
        return batteryPercentage *  EvSafeServiceConfigs.Defaults.MAX_RANGE;
    }

}