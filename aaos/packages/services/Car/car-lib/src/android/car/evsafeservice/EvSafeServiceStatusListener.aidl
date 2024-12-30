// path: /home/chank/aaos/packages/services/Car/car-lib/src/android/car/evsafeservice/

package android.car.evsafeservice;

interface EvSafeServiceStatusListener {
    /**
     * Called when vehicle status changes.
     * @param batteryStatus Current battery percentage (0-100)
     * @param batteryLevel Raw battery level value
     * @param rangeStatus Estimated range in kilometers
     * @param speedLevel Current speed in km/h
     * @param gearStatus Current gear position (P,R,N,D)
     * @param speedWarning Whether speed warning is active
     */
    oneway void onStatusChanged(float batteryStatus, 
                              float batteryLevel,
                              float rangeStatus, 
                              float speedLevel,
                              String gearStatus, 
                              boolean speedWarning);
}