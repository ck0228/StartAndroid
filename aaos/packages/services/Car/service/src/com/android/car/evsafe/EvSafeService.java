package com.android.car.evsafe;

import android.app.Service;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.property.CarPropertyManager;
import android.car.hardware.CarPropertyValue;
import android.car.VehiclePropertyIds;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.android.car.CarService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class EvSafeService extends CarService {
    private Car mCar;
    private CarPropertyManager mCarPropertyManager;
    private CarPropertyManager.CarPropertyEventCallback mGearStatusCallback;
    private CarPropertyManager.CarPropertyEventCallback mBatteryStatusCallback;

    private final GearStatusListener mGearStatusListener = new GearStatusListener() {
        @Override
        public void onGearStatusChanged(int gear) {
            Log.i("EvSafeService", "Gear status changed to: " + gear);
        }
    };

    private final BatteryStatusListener mBatteryStatusListener = new BatteryStatusListener() {
        @Override
        public void onBatteryStatusChanged(int level) {
            Log.i("EvSafeService", "Battery status changed to: " + level);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mCar = Car.createCar(this);
            mCarPropertyManager = (CarPropertyManager) mCar.getCarManager(Car.PROPERTY_SERVICE);
            registerGearStatusListener(mGearStatusListener);
            registerBatteryStatusListener(mBatteryStatusListener);
        } catch (ClassCastException e) {
            Log.e("EvSafeService", "Failed to cast CarManager to CarPropertyManager", e);
            mCarPropertyManager = null;
        } catch (CarNotConnectedException e) {
            Log.e("EvSafeService", "Failed to connect to car service", e);
        }
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
                Log.i("EvSafeService", "Error event received for property: " + propertyId);
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
                Log.i("EvSafeService", "Error event received for property: " + propertyId);
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