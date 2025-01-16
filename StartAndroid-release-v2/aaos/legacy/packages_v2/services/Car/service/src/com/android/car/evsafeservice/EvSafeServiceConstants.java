/**
 * Constants used by EVSafeService for managing EV (Electric Vehicle) status monitoring.
 * Includes constants for notifications, battery management, speed calculations,
 * vehicle status thresholds and default values.
 */
package com.android.car.evsafeservice;

import android.car.VehicleGear;
import android.car.VehiclePropertyIds;

/**
 * Constants and utilities for EV Safe Service.
 * Provides centralized access to configuration values and thresholds.
 */
public final class EvSafeServiceConstants {
    private EvSafeServiceConstants() {
        throw new AssertionError("No instances");
    }

    /** Common logging tag */
    public static final String TAG = "EvSafeServiceConstants";
    
    /** Notification related constants */
    public static final class Notification {
        private Notification() {}
        
        public static final String WARNING_CHANNEL_ID = "EvSafeWarningChannel";
        public static final int WARNING_NOTIFICATION_ID = 1;
        public static final String TITLE = "EV Safety Warning";
        public static final String DESCRIPTION = "Vehicle status warning";
    }

    /** Battery related constants */
    public static final class Battery {
        private Battery() {}
        public static final float PERCENTAGE_MULTIPLIER = 100.0f;
        public static final float MIN_PERCENTAGE = 0.0f;
        public static final float MAX_PERCENTAGE = 100.0f;
    }

    /** Speed calculation related constants */
    public static final class Speed {
        private Speed() {}
        public static final float SPEED_FACTOR_MULTIPLIER = 0.0045f;
        public static final float MIN_SPEED_FACTOR = 0.1f;
        public static final float SPEED_TO_KMH_MULTIPLIER = 3.6f;
        public static final float SPEED_WARNING_THRESHOLD = 100.0f;
    }

    /** Vehicle status thresholds and limits */
    public static final class VehicleStatus {
        private VehicleStatus() {}
        public static final float MAX_RANGE_KM = 450.0f;
        public static final float LOW_BATTERY_THRESHOLD_PERCENT = 20.0f;
        public static final float LOW_RANGE_THRESHOLD_KM = 100.0f;
    }

    /** Sensor sampling rate constants */
    public static final class SensorRate {
        private SensorRate() {}
        // 1Hz sampling rate for both speed and battery
        public static final float SPEED_SAMPLE_RATE = 1.0f;  // CarPropertyManager.SENSOR_RATE_NORMAL
        public static final float BATTERY_SAMPLE_RATE = 1.0f; // CarPropertyManager.SENSOR_RATE_NORMAL
    }

    /** Default values */
    public static final class Defaults {
        public static final String GEAR_STATUS = "Unknown";
        public static final float STATUS_VALUE = 0.0f;
    } 
}