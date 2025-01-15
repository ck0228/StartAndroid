package android.car.evsafeservice;

interface IEvSafeServiceCallback {
    void onSpeedChanged(int speed);
    void onBatteryChanged(int batteryStatus);
    void onGearChanged(String gear);
    void onRangeChanged(int range)
    void onEvSafeServiceEvent(int eventType, in Bundle eventData);
}