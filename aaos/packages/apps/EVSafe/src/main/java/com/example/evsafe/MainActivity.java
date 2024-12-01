package com.example.evsafe;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import android.util.Log;

//11.30 기어 관련 코드 추가: mj
import android.car.Car;
import android.car.hardware.CarSensorManager;
import android.car.hardware.CarSensorEvent;
import android.content.Context;

public class MainActivity extends Activity {
    // 로그 관련 변수 선언
    private static final String TAG = "MainActivity";
    // 기어 관련 변수 선언
    private Car car;
    private CarSensorManager carSensorManager;
    private TextView gearStatusTextView;
    private TextView batteryStatusTextView;
    private TextView speedStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 로그 출력으로 앱 초기화 확인
        Log.d(TAG, "MainActivity onCreate called");

        // 배터리 상태를 표시할 TextView 연결
        batteryStatusTextView = findViewById(R.id.battery_status);
        batteryStatusTextView.setText(""); // 배터리 정보를 공백으로 설정

        // 기어 상태를 표시할 TextView 연결
        gearStatusTextView = findViewById(R.id.gear_status);

        // 속도 상태를 표시할 TextView 연결
        speedStatusTextView = findViewById(R.id.speed_status);

        // 배터리 상태 표시
        displayBatteryLevel();

        // Car API 초기화
        initializeCarApi();        
    }

    // 배터리 상태 표시 메소드 추가: mj
    private void displayBatteryLevel() {
        int batteryLevel = getBatteryLevel();
        Log.d(TAG, "Battery Level: " + batteryLevel + "%");
        batteryStatusTextView.setText("Battery Level: " + batteryLevel + "%");
    }

    // getBatteryLevel 메소드 추가: mj
    private int getBatteryLevel() {
        return -1; // 배터리 정보를 가��올 수 없는 경우
        }
        

    // car api 초기화 메소드
    private void initializeCarApi() {
        car = Car.createCar(this);
        carSensorManager = (CarSensorManager) car.getCarManager(Car.SENSOR_SERVICE);

        if (carSensorManager == null) {
            Log.e(TAG, "CarSensorManager is not available");
            return;
        }

        // 기어, 속도 상태 센서 등록
        carSensorManager.registerListener(
                carSensorListener,
                CarSensorManager.SENSOR_TYPE_GEAR,
                CarSensorManager.SENSOR_RATE_NORMAL
        );

        carSensorManager.registerListener(
                carSensorListener,
                CarSensorManager.SENSOR_RATE_NORMAL,
                CarSensorManager.SENSOR_TYPE_CAR_SPEED
        );
    }

    // 기어(센서) 변경 될때 메소드
    private final CarSensorManager.OnSensorChangedListener carSensorListener =
            new CarSensorManager.OnSensorChangedListener() {
                @Override
                public void onSensorChanged(CarSensorEvent event) {
                    //기어 상태 변경 처리
                    if (event.sensorType == CarSensorManager.SENSOR_TYPE_GEAR) {
                        int gearValue = (int) event.floatValues[0];
                        String gearStatus = getGearStatusText(gearValue);
                        Log.d(TAG, "Gear Value: " + gearValue);
                        gearStatusTextView.setText("Gear Status: " + gearStatus);
                    }
                    //속도 상태 변경 처리
                    if (event.sensorType == carSensorManager.SENSOR_TYPE_CAR_SPEED) {
                        float speedValue = event.floatValues[0];
                        Log.d(TAG, "Speed Value: "+ speedValue + "km/h");
                        speedStatusTextView.setText("Speed"+speedValue+"km/h");
                    }
                }
            };

    // 가져온 기어 데이터를 텍스트로 변환 메소드
    private String getGearStatusText(int gearValue) {
        switch (gearValue) {
            case 0:
                return "Neutral";
            case 1:
                return "Drive";
            case 2:
                return "Reverse";
            case 3:
                return "Park";
            default:
                return "Unknown";
        }
    }

    // 활동 종료 하네? 언제 실행?
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (carSensorManager != null) {
            carSensorManager.unregisterListener(carSensorListener);
        }
        if (car != null) {
            car.disconnect();
        }
    }

}