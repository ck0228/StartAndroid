package com.android.car.evsafe;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.property.CarPropertyManager;
import android.car.hardware.CarPropertyValue;
import android.car.VehiclePropertyIds;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.android.car.CarService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class EvSafeService extends CarService {
    private static final String TAG = "EvSafeService";
    private static EvSafeService instance;

    private Car mCar;
    private CarPropertyManager mCarPropertyManager;
    private CarPropertyManager.CarPropertyEventCallback mGearStatusCallback;
    private CarPropertyManager.CarPropertyEventCallback mBatteryStatusCallback;

    private final GearStatusListener mGearStatusListener = new GearStatusListener() {
        @Override
        public void onGearStatusChanged(int gear) {
            Log.i(TAG, "Gear status changed to: " + gear);
        }
    };

    private final BatteryStatusListener mBatteryStatusListener = new BatteryStatusListener() {
        @Override
        public void onBatteryStatusChanged(int level) {
            Log.i(TAG, "Battery status changed to: " + level);
        }
    };

    @Override
    public void onCreate() {
        Log.i(TAG, "EvSafeService onCreate called");
        super.onCreate();
        
        if (instance != null) {
            Log.i(TAG, "Service is already running");
            return; // 이미 인스턴스가 존재하면 초기화하지 않음
        }

        instance = this;
        Log.i(TAG, "EvSafeService instance created");

        try {
            mCar = Car.createCar(this);
            Log.i(TAG, "Connected to Car Service.");
            mCarPropertyManager = (CarPropertyManager) mCar.getCarManager(Car.PROPERTY_SERVICE);
            registerGearStatusListener(mGearStatusListener);
            registerBatteryStatusListener(mBatteryStatusListener);
        } catch (ClassCastException e) {
            Log.e(TAG, "Failed to cast CarManager to CarPropertyManager", e);
            mCarPropertyManager = null;
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "Failed to connect to car service", e);
        }

        Log.i(TAG, "EvSafeService initialized successfully.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCarPropertyManager != null) {
            mCarPropertyManager.unregisterCallback(mGearStatusCallback);
            mCarPropertyManager.unregisterCallback(mBatteryStatusCallback);
        }
        if (mCar != null) {
            mCar.disconnect();
        }
        instance = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // keep it alive.
        return START_STICKY;
    }

    private void registerGearStatusListener(GearStatusListener listener) {
        mGearStatusCallback = new CarPropertyManager.CarPropertyEventCallback() {
            @Override
            public void onChangeEvent(CarPropertyValue value) {
                if (value.getPropertyId() == VehiclePropertyIds.GEAR_SELECTION) {
                    int gear = (int) value.getValue();
                    listener.onGearStatusChanged(gear);
                }
            }

            @Override
            public void onErrorEvent(int propertyId, int zone) {
                Log.i(TAG, "Error event received for property: " + propertyId);
            }
        };
        mCarPropertyManager.registerCallback(mGearStatusCallback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE);
    }

    private void registerBatteryStatusListener(BatteryStatusListener listener) {
        mBatteryStatusCallback = new CarPropertyManager.CarPropertyEventCallback() {
            @Override
            public void onChangeEvent(CarPropertyValue value) {
                if (value.getPropertyId() == VehiclePropertyIds.EV_BATTERY_LEVEL) {
                    int level = (int) value.getValue();
                    listener.onBatteryStatusChanged(level);
                }
            }

            @Override
            public void onErrorEvent(int propertyId, int zone) {
                Log.i(TAG, "Error event received for property: " + propertyId);
            }
        };
        mCarPropertyManager.registerCallback(mBatteryStatusCallback, VehiclePropertyIds.EV_BATTERY_LEVEL, CarPropertyManager.SENSOR_RATE_ONCHANGE);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        // Dump state
        writer.println("EvSafeService dump:");
        // 추가적인 상태 덤프 로직을 여기에 작성
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}