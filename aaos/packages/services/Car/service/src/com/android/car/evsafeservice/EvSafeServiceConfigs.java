package com.android.car.evsafeservice;

public final class EvSafeServiceConfigs {
    private EvSafeServiceConfigs() {
        throw new AssertionError("No instances");
    }

    /** Common logging tag */
    public static final String TAG = "EvSafeServiceConfigs";
    
    /** Sensor rate */
    public static final float SENSOR_RATE = 1.0f;  // CarPropertyManager.SENSOR_RATE_NORMAL

    /** Thresholds */
    public static final class Thresholds {
        public static final float BATTERY_LOW = 20.0f;
        public static final float BATTERY_CRITICAL = 10.0f;
        public static final float SPEED_WARNING = 100.0f;
    }

    /** Notification related constants */
    public static final class Notification {
        public static final String WARNING_CHANNEL_ID = "EvSafeWarningChannel";
        public static final int WARNING_NOTIFICATION_ID = 1;
    }

    /** Speed calculation related constants */
    public static final float TO_KMH_MULTIPLIER = 3.6f;

    /** Default values */
    public static final class Defaults {
        public static final String GEAR_STATUS = "Unknown";
        public static final float STATUS_VALUE = 0.0f;
        public static final float MAX_RANGE = 600.0f;
    } 
}