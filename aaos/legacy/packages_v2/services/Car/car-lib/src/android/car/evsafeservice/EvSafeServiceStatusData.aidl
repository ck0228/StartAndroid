// path: src/android/car/evsafeservice/EvSafeServiceStatusData.aidl
package android.car.evsafeservice;

/**
 * Listener interface for receiving EV (Electric Vehicle) status updates.
 * Implemented by clients who want to monitor vehicle status changes.
 * @hide
 */
parcelable EvSafeServiceStatusData {
    float batteryStatus;     // 0-100%
    float batteryLevel;      // Raw value
    float rangeStatus;       // km
    float speedLevel;        // km/h
    String gearStatus;       // P,R,N,D
    boolean speedWarning;    // true if speed exceeds threshold
}