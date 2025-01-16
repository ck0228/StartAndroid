package android.car.evsafeservice;

import android.car.evsafeservice.IEvSafeServiceCallback;


interface IEvSafeService {
    void registerCallback(IEvSafeServiceCallback callback);
    void unregisterCallback(IEvSafeServiceCallback callback);
    void updateEvSafeServiceConfig(in Bundle config);
    boolean isServiceReady();
}