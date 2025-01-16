package com.android.car.evsafeservice;

import java.util.Objects;

/**
 * Data class representing the current status of an electric vehicle
 */
public class EvSafeServiceData {
    private static final String TAG = "EvSafeServiceData";

    private String gearStatus;
    private float speed;
    private float batteryStatus;
    private float range;
    private float batteryLevel;
    private boolean isSpeedWarning;

    /**
     * Creates a new EvSafeServiceData with default values
     */
    public EvSafeServiceData() {
        this.gearStatus = EvSafeServiceConfigs.Defaults.GEAR_STATUS;
        this.speed = EvSafeServiceConfigs.Defaults.STATUS_VALUE;
        this.batteryStatus = EvSafeServiceConfigs.Defaults.STATUS_VALUE;
        this.range = EvSafeServiceConfigs.Defaults.STATUS_VALUE;
        this.batteryLevel = EvSafeServiceConfigs.Defaults.STATUS_VALUE;
        this.isSpeedWarning = false;
    }

    /**
     * Creates a new EvSafeServiceData by copying another instance
     * @param other The EvSafeServiceData to copy from
     * @throws NullPointerException if other is null
     */
    public EvSafeServiceData(EvSafeServiceData other) {
        Objects.requireNonNull(other, "Source EvSafeServiceData cannot be null");
        this.gearStatus = other.gearStatus;
        this.speed = other.speed;
        this.batteryStatus = other.batteryStatus;
        this.range = other.range;
        this.batteryLevel = other.batteryLevel;
        this.isSpeedWarning = other.isSpeedWarning;
    }

    /** Getters */
    public String getGearStatus() {
        return gearStatus;
    }

    public float getSpeed() {
        return speed;
    }

    public float getBatteryStatus() {
        return batteryStatus;
    }

    public float getRange() {
        return range;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public boolean getSpeedWarning() {
        return isSpeedWarning;
    }

    /** Setters */
    public void setGearStatus(String gearStatus) {
        this.gearStatus = gearStatus;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setSpeedWarning(boolean isWarning) {
        this.isSpeedWarning = isWarning;
    }

    public void setBatteryStatus(float batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public void setBatteryLevel(float batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvSafeServiceData evsData = (EvSafeServiceData) o;
        return Float.compare(evsData.speed, speed) == 0 &&
                Float.compare(evsData.batteryStatus, batteryStatus) == 0 &&
                Float.compare(evsData.range, range) == 0 &&
                Float.compare(evsData.batteryLevel, batteryLevel) == 0 &&
                evsData.isSpeedWarning == isSpeedWarning &&
                Objects.equals(gearStatus, evsData.gearStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gearStatus, speed, batteryStatus, 
                          range, batteryLevel, isSpeedWarning);
    }
}