// path: /home/chank/aaos/packages/services/Car/car-lib/src/android/car/evsafeservice/EvSafeServiceApi.aidl

package android.car.evsafeservice;

import android.car.evsafeservice.EvSafeServiceStatusListener;

/**
 * Interface for communicating with the EV Safe Service.
 * Provides methods to monitor and control electric vehicle status information.
 * @hide
 */
interface EvSafeServiceApi {
    /**
     * Broadcast action for EV status updates
     * @hide
     */
    const String ACTION_STATUS_UPDATE = "com.android.car.evsafeservice.ACTION_STATUS_UPDATE";
    const String PERMISSION_RECEIVE_STATUS = "com.android.car.evsafeservice.permission.RECEIVE_STATUS_UPDATE";

    /**
     * Error codes for service operations
     */
    const int STATUS_OK = 0;
    const int ERROR_SERVICE_NOT_READY = -1;
    const int ERROR_INVALID_PARAMETER = -2;

    // Service lifecycle methods
    /**
     * Start the EV Safe Service
     * @throws ServiceException if service fails to start
     */
    void startService();

    /**
     * Stop the EV Safe Service
     * @throws ServiceException if service fails to stop properly
     */
    void stopService();

    // Status query methods
    /**
     * Get current battery percentage
     * @return battery level as percentage (0-100)
     * @throws ServiceException if value cannot be retrieved
     */
    float getBatteryStatus();

    /**
     * Get raw battery level value
     * @return raw battery level
     * @throws ServiceException if value cannot be retrieved
     */
    float getBatteryLevel();

    /**
     * Get estimated driving range
     * @return range in kilometers
     * @throws ServiceException if value cannot be retrieved
     */
    float getRangeStatus();

    /**
     * Get current vehicle speed
     * @return speed in km/h
     * @throws ServiceException if value cannot be retrieved
     */
    float getSpeedLevel();

    /**
     * Get current gear position
     * @return gear status (P,R,N,D)
     * @throws ServiceException if value cannot be retrieved
     */
    String getGearStatus();

    /**
     * Get speed warning status
     * @return true if speed warning is active
     * @throws ServiceException if value cannot be retrieved
     */
    boolean getSpeedWarning();

    // Broadcast control methods
    /**
     * Enable status broadcast updates
     * @throws ServiceException if broadcasts cannot be enabled
     */
    void enableBroadcasts();

    /**
     * Disable status broadcast updates
     * @throws ServiceException if broadcasts cannot be disabled
     */
    void disableBroadcasts();

    // Listener registration methods
    /**
     * Register status change listener
     * @param listener callback to receive updates
     * @throws IllegalArgumentException if listener is null
     */
    void registerListener(in EvSafeServiceStatusListener listener); // XXX: in 추가됨

    /**
     * Unregister status change listener
     * @param listener callback to remove
     * @throws IllegalArgumentException if listener is null
     */
    void unregisterListener(in EvSafeServiceStatusListener listener); // XXX: in 추가됨
}