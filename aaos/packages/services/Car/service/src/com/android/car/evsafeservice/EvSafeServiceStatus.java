package com.android.car.evsafeservice;

import java.util.Objects;

/**
 * Data class representing the current status of an electric vehicle
 */
public class EvSafeServiceStatus {
    public static final String TAG = "EvSafeServiceStatus";
    private String gearStatus;
    private float speedStatus;
    private float batteryStatus;
    private float rangeStatus;

    /**
     * Creates a new EvStatus with default values
     */
    public EvSafeServiceStatus() {
        this.gearStatus = EvSafeServiceConstants.Defaults.GEAR_STATUS;
        this.speedStatus = EvSafeServiceConstants.Defaults.STATUS_VALUE;
        this.batteryStatus = EvSafeServiceConstants.Defaults.STATUS_VALUE;
        this.rangeStatus = EvSafeServiceConstants.Defaults.STATUS_VALUE;
    }

    /**
     * Creates a new EvStatus by copying another instance
     * @param other The EvStatus to copy from
     * @throws NullPointerException if other is null
     */
    public EvSafeServiceStatus(EvSafeServiceStatus other) {
        Objects.requireNonNull(other, "Source EvStatus cannot be null");
        this.gearStatus = other.gearStatus;
        this.speedStatus = other.speedStatus;
        this.batteryStatus = other.batteryStatus;
        this.rangeStatus = other.rangeStatus;
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
    public float getSpeedStatus() {
        return speedStatus;
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
     * Sets the gear status
     * @param gearStatus The new gear status
     * @throws NullPointerException if gearStatus is null
     */
    public void setGearStatus(String gearStatus) {
        this.gearStatus = Objects.requireNonNull(gearStatus, "Gear status cannot be null");
    }

    /**
     * Sets the vehicle speed
     * @param speedStatus Speed in km/h
     * @throws IllegalArgumentException if speed is negative
     */
    public void setSpeedStatus(float speedStatus) {
        if (speedStatus < 0) {
            throw new IllegalArgumentException("Speed cannot be negative");
        }
        this.speedStatus = speedStatus;
    }

    /**
     * Sets the battery level
     * @param batteryStatus Battery percentage (0-100)
     * @throws IllegalArgumentException if not in valid range
     */
    public void setBatteryStatus(float batteryStatus) {
        if (batteryStatus < 0 || batteryStatus > EvSafeServiceConstants.Battery.PERCENTAGE_MULTIPLIER) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvSafeServiceStatus evsStatus = (EvSafeServiceStatus) o;
        return Float.compare(evsStatus.speedStatus, speedStatus) == 0 &&
                Float.compare(evsStatus.batteryStatus, batteryStatus) == 0 &&
                Float.compare(evsStatus.rangeStatus, rangeStatus) == 0 &&
                Objects.equals(gearStatus, evsStatus.gearStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gearStatus, speedStatus, batteryStatus, rangeStatus);
    }

    @Override
    public String toString() {
        return String.format(
            "Status{gear=%s, speed=%.1f, battery=%.1f%%, range=%.1f}",
            gearStatus,
            speedStatus,
            batteryStatus,
            rangeStatus
        );
    }
}