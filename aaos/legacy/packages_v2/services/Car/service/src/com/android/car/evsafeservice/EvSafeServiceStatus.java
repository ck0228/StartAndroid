package com.android.car.evsafeservice;

import java.util.Objects;

/**
 * Data class representing the current status of an electric vehicle
 */
public class EvSafeServiceStatus {
    public static final String TAG = "EvSafeServiceStatus";

    private String gearStatus;
    private float speedLevel;
    private float batteryStatus;
    private float rangeStatus;
    private float batteryLevel;
    private boolean isSpeedWarning;

    /**
     * Creates a new EvStatus with default values
     */
    public EvSafeServiceStatus() {
        this.gearStatus = EvSafeServiceConstants.Defaults.GEAR_STATUS;
        this.speedLevel = EvSafeServiceConstants.Defaults.STATUS_VALUE;
        this.batteryStatus = EvSafeServiceConstants.Defaults.STATUS_VALUE;
        this.batteryLevel = EvSafeServiceConstants.Defaults.STATUS_VALUE;
        this.rangeStatus = EvSafeServiceConstants.VehicleStatus.MAX_RANGE_KM;
        this.batteryLevel = EvSafeServiceConstants.Defaults.STATUS_VALUE;
        this.isSpeedWarning = false;
    }

    /**
     * Creates a new EvStatus by copying another instance
     * @param other The EvStatus to copy from
     * @throws NullPointerException if other is null
     */
    public EvSafeServiceStatus(EvSafeServiceStatus other) {
        Objects.requireNonNull(other, "Source EvStatus cannot be null");
        this.gearStatus = other.gearStatus;
        this.speedLevel = other.speedLevel;
        this.batteryStatus = other.batteryStatus;
        this.batteryLevel = other.batteryLevel;
        this.rangeStatus = other.rangeStatus;
        this.batteryLevel = other.batteryLevel;
        this.isSpeedWarning = other.isSpeedWarning;
    }

    /**
     * @return Current gear position
     */
    public String getGearStatus() {
        return String.valueOf(gearStatus);
    }

    /**
     * @return Current speed in km/h
     */
    public float getSpeedLevel() {
        return speedLevel;
    }

    /**
     * @return Current battery level percentage
     */
    public float getBatteryStatus() {
        return batteryStatus;
    }

    /**
     * @return Current estimated range in km
     */
    public float getRangeStatus() {
        return rangeStatus;
    }

    /**
     * @return Current battery level
     */
    public float getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * @return Current speed warning status
     */
    public boolean getSpeedWarning() {
        return isSpeedWarning;
    }

    /**
     * Sets the gear status
     * @param gearStatus The new gear status
     * @throws NullPointerException if gearStatus is null
     */
    public void setGearStatus(String gearStatus) {
        this.gearStatus = Objects.requireNonNull(gearStatus, "Gear status cannot be null");
    }

    /**
     * Sets the vehicle speed
     * @param speedLevel Speed in km/h
     * @throws IllegalArgumentException if speed is negative
     */
    public void setSpeedLevel(float speedLevel) {
        if (speedLevel < 0) {
            throw new IllegalArgumentException("Speed cannot be negative");
        }
        this.speedLevel = speedLevel;
    }

    /**
     * Sets the speed warning status
     * @param isWarning New warning status
     */
    public void setSpeedWarning(boolean isWarning) {
        this.isSpeedWarning = isWarning;
    }

    /**
     * Sets the battery level
     * @param batteryStatus Battery percentage (0-100)
     * @throws IllegalArgumentException if not in valid range
     */
    public void setBatteryStatus(float batteryStatus) {
        if (batteryStatus < EvSafeServiceConstants.Battery.MIN_PERCENTAGE || 
            batteryStatus > EvSafeServiceConstants.Battery.MAX_PERCENTAGE) {
            throw new IllegalArgumentException("Battery status must be between 0 and 100");
        }
        this.batteryStatus = batteryStatus;
    }

    /**
     * Sets the estimated range
     * @param rangeStatus Range in km
     * @throws IllegalArgumentException if range is negative or exceeds maximum
     */
    public void setRangeStatus(float rangeStatus) {
        if (rangeStatus < 0 || rangeStatus > EvSafeServiceConstants.VehicleStatus.MAX_RANGE_KM) {
            throw new IllegalArgumentException(
                String.format("Range must be between 0 and %.1f km", 
                EvSafeServiceConstants.VehicleStatus.MAX_RANGE_KM));
        }
        this.rangeStatus = rangeStatus;
    }

    /**
     * Sets the battery level
     * @param batteryLevel Battery level value
     * @throws IllegalArgumentException if not in valid range
     */
    public void setBatteryLevel(float batteryLevel) {
        if (batteryLevel < 0) {
            throw new IllegalArgumentException(
                String.format("Battery level must be bigger then 0"));
        }
        this.batteryLevel = batteryLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvSafeServiceStatus)) return false;
        EvSafeServiceStatus that = (EvSafeServiceStatus) o;
        return Float.compare(that.speedLevel, speedLevel) == 0 &&
               Float.compare(that.batteryStatus, batteryStatus) == 0 &&
               Float.compare(that.batteryLevel, batteryLevel) == 0 &&
               Float.compare(that.rangeStatus, rangeStatus) == 0 &&
               isSpeedWarning == that.isSpeedWarning &&
               Objects.equals(gearStatus, that.gearStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gearStatus, speedLevel, batteryStatus, 
                          rangeStatus, batteryLevel, isSpeedWarning);
    }
}