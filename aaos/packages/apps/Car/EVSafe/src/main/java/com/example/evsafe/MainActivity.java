package com.example.evsafe;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager; //Toast 기능 위한 추가: mj
import android.app.Notification; // notification 기능 추가: mj
import android.app.NotificationChannel; // notification 기능 추가: mj
import android.app.NotificationManager; // notification 기능 추가: mj
import android.app.PendingIntent; // notification 기능 추가: mj
import android.app.usage.UsageStats; // 추가: UsageStats 클래스
import android.app.usage.UsageStatsManager; // 추가: UsageStatsManager 클래스
import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.VehicleGear;
import android.car.hardware.property.CarPropertyManager;
import android.content.Context; //Toast 기능 위한 추가: mj
import android.content.Intent; // notification 기능 추가: mj
import android.content.pm.ApplicationInfo; //Toast 기능 위한 추가: mj
import android.content.pm.PackageManager; //Toast 기능 위한 추가: mj
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast; //Toast 기능 위한 추가: mj

public class MainActivity extends Activity {
    // Constants
    private static final String TAG = "EVSafe";
    private static final int UPDATE_INTERVAL_MS = 1000;
    private static final String CHANNEL_ID = "speed_warning_channel";
    private static final int NOTIFICATION_ID = 1;

    // Car related members
    private Car car;
    private CarPropertyManager propertyManager;
    
    // UI elements
    private TextView gearText;
    private TextView batteryText;
    private TextView rangeText;
    private TextView speedText;

    // Update handler
    private Handler handler;

    // 플래그 변수 선언
    private boolean speedWarningSent = false; // 속도 경고 플래그
    private boolean toastDisplayed = false;  // 토스트 플래그

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateVehicleStatus();
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        handler = new Handler(Looper.getMainLooper());
        initViews();
        initCar();
        createNotificationChannel(); // Notification 채널 초기화: mj
    }

    private void initViews() {
        gearText = findViewById(R.id.gear_status);
        batteryText = findViewById(R.id.battery_status);
        rangeText = findViewById(R.id.range_status);
        speedText = findViewById(R.id.speed_status);        
    }

    private void initCar() {
        try {
            car = Car.createCar(this);
            propertyManager = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create car manager", e);
        }
    }

    private void updateVehicleStatus() {
        if (propertyManager == null) {
            Log.e(TAG, "PropertyManager is null");
            return;
        }    
        updateGearStatus();
        updateBatterySpeedStatus();
        checkAndToastDrivingState();
    }
    
    private void updateGearStatus() {
        try {
            int gear = propertyManager.getIntProperty(
                VehiclePropertyIds.GEAR_SELECTION, 
                0
            );
            gearText.setText("Gear: " + getGearString(gear));
            if (gear == VehicleGear.GEAR_PARK) {
                toastDisplayed = false; // 기어가 P일 때 플래그 초기화
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for gear reading", e);
            gearText.setText("Gear: No permission");
        } catch (Exception e) {
            Log.e(TAG, "Error reading gear", e);
            gearText.setText("Gear: Error");
        }
    }
    
    private void updateBatterySpeedStatus() {        
        try {
            float speedMs = propertyManager.getFloatProperty(
                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                0
            );
            float batteryLevel = propertyManager.getFloatProperty(
                VehiclePropertyIds.EV_BATTERY_LEVEL,
                0
            );
            float batteryCapacity = propertyManager.getFloatProperty(
                VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY,
                0
            );              
            float speedKmh = convertMsToKmh(speedMs);
            float batteryPercentage = calculateBatteryPercentage(batteryLevel, batteryCapacity);
            float estimatedRange = calculateEstimatedRange(batteryPercentage, speedKmh); // 주행거리 계산
            Log.d(TAG, "Battery Level: " + batteryLevel + 
                  " Capacity: " + batteryCapacity + 
                  " Percentage: " + batteryPercentage + "%"+
                  "Speed(m/s): " + speedMs + " -> Speed(km/h): " + speedKmh +
                  "Estimated Range: " + estimatedRange + " km");            
            batteryText.setText(String.format("Battery: %.0f%%", batteryPercentage));
            speedText.setText(String.format("Speed: %.0f km/h", speedKmh));
            rangeText.setText(String.format("Range: %.0f km", estimatedRange));
            // 속도 초과 시 알림 전송
            if (speedKmh > 150) {
                try {
                    sendSpeedWarningNotification(); // 속도 초과 알림
                } catch (Exception e) {
                    Log.e(TAG, "Error sending speed warning notification", e);
                    // 알림 오류 발생 시에도 UI 업데이트는 계속 진행
                }
            } else {
                speedWarningSent = false; // 속도가 낮아지면 플래그 초기화
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for battery or speed reading", e);
            batteryText.setText("Battery : No permission");
            speedText.setText("Speed: No permission");
        } catch (Exception e) {
            Log.e(TAG, "Error reading battery or speed", e);
            batteryText.setText("Battery: Error");
            speedText.setText("Speed: Error");
        }
    }

    //주행중 토스트 기능 추가: mj
    private void checkAndToastDrivingState() {
        if (toastDisplayed) return; // 이미 표시된 경우 중단

        try {
            int gear = propertyManager.getIntProperty(VehiclePropertyIds.GEAR_SELECTION, 0);
            float speedMs = propertyManager.getFloatProperty(VehiclePropertyIds.PERF_VEHICLE_SPEED, 0);

            // 속도가 0, 특정 앱, 기어가 R이나 D일 때 Toast 메시지 출력
            if (speedMs == 0 && (gear == VehicleGear.GEAR_REVERSE || gear == VehicleGear.GEAR_DRIVE) && isAppRunning("com.google.android.car.garagemode.testapp")) {
                Toast.makeText(this, "주행 중에는 앱 사용이 제한됩니다.", Toast.LENGTH_LONG).show();
                toastDisplayed = true; // 플래그 설정
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for gear reading", e);
        } catch (Exception e) {
            Log.e(TAG, "Error checking gear and speed", e);
        }
    }

    private String getGearString(int gear) {
        if (gear == VehicleGear.GEAR_PARK) return "P";
        if (gear == VehicleGear.GEAR_REVERSE) return "R";
        if (gear == VehicleGear.GEAR_NEUTRAL) return "N";
        if (gear == VehicleGear.GEAR_DRIVE) return "D";
        return "Unknown";
    }

    private float convertMsToKmh(float speedMs) {
        return speedMs * 3.6f; // m/s * (3600/1000) = km/h
    }

    private float calculateBatteryPercentage(float batteryLevel, float batteryCapacity) {
        if (batteryCapacity <= 0) {
            Log.w(TAG, "Invalid battery capacity: " + batteryCapacity);
            return 0.0f;
        }
        return (batteryLevel / batteryCapacity) * 100.0f;
    }

    // 배터리와 속도에 따른 주행 가능 거리 계산 함수
    private float calculateEstimatedRange(float batteryPercentage, float currentSpeed) {
        final float MAX_RANGE = 450.0f; // 100%일 때 최대 주행거리 (km)
        float speedFactor = calculateSpeedFactor(currentSpeed); // 속도에 따른 가중치 계산
        return (batteryPercentage / 100.0f) * MAX_RANGE * speedFactor;
    }

    // 속도에 따른 가중치 계산 함수 : mj
    private float calculateSpeedFactor(float speed) {
        if (speed < 80) {
            return 1.0f;
        } else if (speed <= 100) {
            return 0.7f;
        } else {
            return 0.5f;
        }
    }

    // notification 기능 추가: mj
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Speed Warning";
            String description = "Notifies when the vehicle speed exceeds 150 km/h.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // notification 기능 추가: mj
    private void sendSpeedWarningNotification() {
        if (speedWarningSent) return; // 이미 전송된 경우 중단

        try{
            Intent intent = new Intent(this, MainActivity.class); // 클릭 시 메인 액티비티로 이동
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
             PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); // Android 12 이상 호환(Immutable)

            Notification.Builder builder = new Notification.Builder(this) //channel id 아래 선언
                    .setSmallIcon(R.drawable.ic_warning) // 알림 아이콘
                    .setContentTitle("과속 경고")
                    .setContentText("속도가 너무 빠릅니다! 과속 주의!")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true); // 알림 클릭 시 자동 삭제

            // Android 8.0 이상에서는 채널 ID가 필요
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(CHANNEL_ID);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
                speedWarningSent = true; // 플래그 설정
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to send notification", e);
        }
    }

    // 앱이 실행중 확인하는 함수: mj
    private boolean isAppRunning(String packageName) {
        Log.d(TAG, "Checking if app is running: " + packageName);
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            Log.e(TAG, "UsageStatsManager is null");
            return false;
        }
        long currentTime = System.currentTimeMillis();
        Log.d(TAG, "Current time: " + currentTime);
        // 최근 5초간 실행된 앱 확인
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, 
            currentTime - 10000, 
            currentTime
        );
        if (stats != null) {
            for (UsageStats usageStats : stats) {
                Log.d(TAG, "Package: " + usageStats.getPackageName() +
                ", Last time used: " + usageStats.getLastTimeUsed());

                if (usageStats.getPackageName().equals(packageName)) {
                    return true;
                }
            }
            Log.d(TAG, "App is not running: " + packageName);
        }       
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onDestroy() {
        if (car != null) {
            car.disconnect();
        }
        super.onDestroy();
    }
}