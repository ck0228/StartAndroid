// path: src/android/car/evsafeservice/EvSafeServiceStatusListener.aidl
package android.car.evsafeservice;

import android.car.evsafeservice.EvSafeServiceStatusData;

interface EvSafeServiceStatusListener {
    /**
     * Called when vehicle status changes.
     * All status updates are delivered asynchronously.
     * 
     * @param status Bundle containing all vehicle status information
     * @throws RemoteException if communication fails
     */
    oneway void onStatusChanged(in EvSafeServiceStatusData status);

    /**
     * Called when an error occurs during status monitoring.
     * 
     * @param errorCode Error code indicating failure reason
     * @param message Detailed error description
     */
    oneway void onError(int errorCode, String message);
}