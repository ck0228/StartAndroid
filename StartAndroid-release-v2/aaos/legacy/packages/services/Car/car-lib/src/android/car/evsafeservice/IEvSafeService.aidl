package android.car.evsafeservice;

import android.os.IBinder;

/**
 * @hide
 */
interface IEvSafeService {
    int getCurrentGear();
    boolean isInPark();
}
