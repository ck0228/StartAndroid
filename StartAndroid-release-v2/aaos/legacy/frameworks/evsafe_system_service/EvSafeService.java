package com.android.server;
import android.app.EvSafeServiceManager;
import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import android.content.Context;
import com.android.server.SystemService;

// For Car API
import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.VehicleGear;
import android.car.hardware.property.CarPropertyManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class EvSafeService extends IEvSafeServiceManager.Stub {
    private final static String LOG_TAG = "EvSafeService";
    private static final int UPDATE_INTERVAL_MS = 1000;
 
    private final Object mLock = new Object();

    private final Context mContext;
    private Car car;
    private CarPropertyManager propertyManager;
    private Handler handler;
 
    private final Context mContext;
    
    EvSafeService(Context context) {
        mContext = context;
    }   
    
    @Override
    public void printEvSafe() throws RemoteException {
        Slog.i(LOG_TAG,"EvSafe,World!");
    }   
}
