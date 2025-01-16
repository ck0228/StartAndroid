package android.app;

// File: frameworks/base/core/java/android/app

import android.annotation.SdkConstant;
import android.annotation.SystemApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.IEvSafeService;
import android.util.Log;


public class EvSafeServiceManager {
    private static final String TAG = "EvSafeServiceManager";
    private final IEvSafeService mService;

    pubilc void startEvSafe(){
        try{
            return mService.startEvSafe();
        } catch (Exception e){
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return 0;
    }
}
