package com.example.evsafe;

import android.os.Bundle;
import android.app.Activity;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.property.CarPropertyEventCallback;
import android.car.hardware.property.CarPropertyManager;
import android.util.Log;

public class MainActivity extends Activity {
    private Car car;
    private CarPropertyManager carPropertyManager;
    private static final int PERF_VEHICLE_SPEED = 291504647; // 0x11600207

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        car = Car.createCar(this);
        carPropertyManager = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);

        try {
            carPropertyManager.registerCallback(new CarPropertyEventCallback() {
                @Override
                public void onChangeEvent(CarPropertyManager.CarPropertyEvent event) {
                    if (event.getPropertyId() == PERF_VEHICLE_SPEED) {
                        float vehicleSpeed = (float) event.getCarPropertyValue().getValue();
                        Log.d("MainActivity", "Vehicle Speed: " + vehicleSpeed);
                    }
                }

                @Override
                public void onErrorEvent(int propertyId, int zone) {
                    if (propertyId == PERF_VEHICLE_SPEED) {
                        Log.e("MainActivity", "Error reading vehicle speed");
                    }
                }
            }, PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_ONCHANGE);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (car != null) {
            car.disconnect();
        }
    }
}



