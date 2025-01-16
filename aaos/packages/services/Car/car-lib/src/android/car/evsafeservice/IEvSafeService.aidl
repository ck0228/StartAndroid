package android.car.evsafeservice;

import android.car.evsafeservice.IEvSafeServiceCallback;
import android.os.Bundle;

interface IEvSafeService {
    void registerCallback(IEvSafeServiceCallback callback);
    void unregisterCallback(IEvSafeServiceCallback callback);
    boolean isServiceReady();
}