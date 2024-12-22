// path: /home/chank/aaos/packages/services/Car/car-lib/src/android/car/evsafeservice/EvSafeServiceApi.aidl

package android.car.evsafeservice;

interface EvSafeServiceApi {
    void startService();
    void stopService();
    float getBatteryStatus();
    float getRangeStatus();
    float getSpeedStatus();
    String getGearStatus();
}