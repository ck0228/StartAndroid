// path: /home/chank/aaos/packages/services/Car/car-lib/src/android/car/evsafeservice/EvSafeServiceApi.aidl

package android.car.evsafeservice;

import android.car.evsafeservice.EvSafeServiceStatusListener;

interface EvSafeServiceApi {
    void startService();
    void stopService();
    float getBatteryStatus();
    float getBatteryLevel();
    float getRangeStatus();
    float getSpeedLevel();
    String getGearStatus();
    boolean getSpeedWarning();

    void enableBroadcasts();
    void disableBroadcasts();

    void registerListener(EvSafeServiceStatusListener listener);
    void unregisterListener(EvSafeServiceStatusListener listener);

    const String ACTION_EV_STATUS_UPDATE = "com.android.car.evsafeservice.ACTION_STATUS_UPDATE";
}