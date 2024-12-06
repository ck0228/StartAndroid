package com.android.car.evsafeservice;

import android.provider.Settings;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.VehicleGear;
import android.car.hardware.property.CarPropertyManager;
import android.os.Handler;
import android.os.Looper;
import android.app.ActivityManager; // Toast 기능 위한 추가: mj
import android.content.pm.ApplicationInfo; // Toast 기능 위한 추가: mj
import android.content.pm.PackageManager; // Toast 기능 위한 추가: mj
import android.widget.Toast; // Toast 기능 위한 추가: mj
import android.app.usage.UsageStats; // 추가: UsageStats 클래스
import android.app.usage.UsageStatsManager; // 추가: UsageStatsManager 클래스
import android.app.AppOpsManager;

import java.util.List; // List import 추가
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class EvSafeService extends Service {
    // Service identification constants
    private static final String TAG = "EvSafeService";
    private static final int UPDATE_INTERVAL_MS = 1000;

    private static final String SERVICE_CHANNEL_ID = "EvSafeServiceChannel";
    private static final String WARNING_CHANNEL_ID = "EvSafeWarningChannel";
    private static final String SERVICE_CHANNEL_NAME = "EV Safe Service Channel";
    private static final String WARNING_CHANNEL_NAME = "EV Safe Warning Channel";
    private static final String SERVICE_CHANNEL_DESC = "Monitors EV status";
    private static final String WARNING_CHANNEL_DESC = "Shows important warnings";
    private static final int SERVICE_NOTIFICATION_ID = 1;
    private static final int WARNING_NOTIFICATION_ID = 2;
    
    // Warning threshold constants
    private static final float BATTERY_WARNING_THRESHOLD = 20.0f; // Warning below 00%
    private static final float RANGE_WARNING_THRESHOLD = 100.0f;   // Warning below 00km
    
    // Status tracking flags
    private boolean isLowBatteryWarningShown = false;
    private boolean isLowRangeWarningShown = false;
    private boolean isServiceRunning = false;

    // Add these fields to the class
    private String previousGearStatus = "Unknown";
    private float previousSpeedStatus = 0.0f;
    private float previousBatteryStatus = 0.0f;
    private float previousRangeStatus = 0.0f;

    // Vehicle status variables
    private String gearStatus = "Unknown";
    private float speedStatus = 0.0f;
    private float batteryStatus = 0.0f;
    private float rangeStatus = 0.0f;

    // System services
    private NotificationManager notificationManager;
    private Car car;
    private CarPropertyManager propertyManager;
    private Handler handler;
    

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "1. Service onCreate");
        
        // Initialize notification manager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        
        // Initialize handler on main thread
        handler = new Handler(Looper.getMainLooper());
        
        // Initialize car systems
        initCar();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "2. Service onStartCommand");
        
        if (!isServiceRunning) {
            isServiceRunning = true;
            Log.d(TAG, "3. isServiceRunning to true");
            startForeground(SERVICE_NOTIFICATION_ID, createNotification());
            handler.post(updateRunnable);
            Log.d(TAG, "4. Successfully started service");
        }
        
        checkUsageStatsPermission(this);
        
        return START_STICKY;
    }

    // Status update runnable
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "RUN1. Updating vehicle status...");
            updateVehicleStatus();
            
            // Check if any status has changed
            if (hasStatusChanged()) {

                
                Log.d(TAG, "RUN2. Status changed, Updating notification");
                updateNotification();
                updatePreviousStatus();

                Log.d(TAG, "RUN3. Check Warning Condition and Notify if needed");
                checkWarningConditions();
            }

            checkAndStopEvSafeApp();
            
            if (isServiceRunning) {
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        }
    };


    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, EvSafeService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );
    
        String contentText = String.format(
            "Battery: %.1f%% | Range: %.1fkm | Speed: %.1fkm/h | Gear: %s",
            batteryStatus, rangeStatus, speedStatus, gearStatus
        );
        
        return new Notification.Builder(this, SERVICE_CHANNEL_ID)
            .setContentTitle("EV Safe Service")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build();
    }

    private void showWarningNotification(String title, String message) {
        Intent intent = new Intent(this, EvSafeService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        );
    
        Notification notification = new Notification.Builder(this, WARNING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build();
    
        if (notificationManager != null) {
            notificationManager.notify(WARNING_NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        // Service channel (LOW importance)
        NotificationChannel serviceChannel = new NotificationChannel(
            SERVICE_CHANNEL_ID,
            SERVICE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        );
        serviceChannel.setDescription(SERVICE_CHANNEL_DESC);
        serviceChannel.setShowBadge(false);
        serviceChannel.enableLights(false);
        serviceChannel.enableVibration(false);
        notificationManager.createNotificationChannel(serviceChannel);
    
        // Warning channel (HIGH importance)
        NotificationChannel warningChannel = new NotificationChannel(
            WARNING_CHANNEL_ID,
            WARNING_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        );
        warningChannel.setDescription(WARNING_CHANNEL_DESC);
        warningChannel.setShowBadge(true);
        warningChannel.enableLights(true);
        warningChannel.enableVibration(true);
        notificationManager.createNotificationChannel(warningChannel);
    }

    private void updateNotification() {
        if (isServiceRunning) {
            notificationManager.notify(SERVICE_NOTIFICATION_ID, createNotification());
        }
    }

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, EvSafeService.class);
        context.startForegroundService(serviceIntent);
    }

    private void initCar() {
        try {
            car = Car.createCar(this);
            propertyManager = (CarPropertyManager) car.getCarManager(Car.PROPERTY_SERVICE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create car manager", e);
        }
    }


    private boolean hasStatusChanged() {
        return !gearStatus.equals(previousGearStatus) ||
            Math.abs(speedStatus - previousSpeedStatus) > 0.1f ||
            Math.abs(batteryStatus - previousBatteryStatus) > 0.1f ||
            Math.abs(rangeStatus - previousRangeStatus) > 0.1f;
    }

    private void updatePreviousStatus() {
        previousGearStatus = gearStatus;
        previousSpeedStatus = speedStatus;
        previousBatteryStatus = batteryStatus;
        previousRangeStatus = rangeStatus;
    }


    private void checkWarningConditions() {
        // Check range warning
        if (rangeStatus < RANGE_WARNING_THRESHOLD && !isLowRangeWarningShown) {
            isLowRangeWarningShown = true;
            showWarningNotification("Range Warning", 
                String.format("주행가능거리가 %.1fkm 남았습니다.", rangeStatus));
        } else if (rangeStatus >= RANGE_WARNING_THRESHOLD) {
            isLowRangeWarningShown = false;
        }
    
        // Check battery warning
        if (batteryStatus < BATTERY_WARNING_THRESHOLD && !isLowBatteryWarningShown) {
            isLowBatteryWarningShown = true;
            showWarningNotification("Battery Warning", 
                String.format("Low battery warning: %.1f%% remaining", batteryStatus));
        } else if (batteryStatus >= BATTERY_WARNING_THRESHOLD) {
            isLowBatteryWarningShown = false;
        }
    }

    private void updateVehicleStatus() {
        if (propertyManager == null) {
            Log.e(TAG, "PropertyManager is null");
            return;
        }
    
        gearStatus = updateGearStatus();
        speedStatus = updateSpeedStatus();
        batteryStatus = updateBatteryStatus();
        rangeStatus = updateRangeStatus(batteryStatus, speedStatus);
    }

    private String updateGearStatus() {
        String gear = "Unknown";

        try {
            int gearInt = propertyManager.getIntProperty(
                VehiclePropertyIds.GEAR_SELECTION,
                0
            );
            return gearString(gearInt);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for gear reading", e);
            return gear;
        } catch (Exception e) {
            Log.e(TAG, "Error reading gear", e);
            return gear;
        }
    }

    private String gearString(int gearInt) {
        switch (gearInt) {
            case VehicleGear.GEAR_PARK: return "P";
            case VehicleGear.GEAR_REVERSE: return "R";
            case VehicleGear.GEAR_NEUTRAL: return "N";
            case VehicleGear.GEAR_DRIVE: return "D";
            default: return "Unknown"; 
        }
    }
    
    private float updateSpeedStatus() {
        float speedKmh = -1;

        try {
            float speedMs = propertyManager.getFloatProperty(
                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                0
            );
            speedKmh = convertMsToKmh(speedMs);

        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for speed reading", e);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Error reading speed", e);
            return -1;
        }
        return speedKmh;
    }

    private float convertMsToKmh(float speedMs) {
        return speedMs * 3.6f; // m/s * (3600/1000) = km/h
    }

    private float updateBatteryStatus() {
        float batteryPercentage = -1;

        try {
            float batteryLevel = propertyManager.getFloatProperty(
                VehiclePropertyIds.EV_BATTERY_LEVEL,
                0
            );
            float batteryCapacity = propertyManager.getFloatProperty(
                VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY,
                0
            );
            
            batteryPercentage = calculateBatteryPercentage(batteryLevel, batteryCapacity);
            
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for battery reading", e);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Error reading battery", e);
            return -1;
        }
        return batteryPercentage;
    }

    private float calculateBatteryPercentage(float batteryLevel, float batteryCapacity) {
        if (batteryCapacity <= 0) {
            Log.w(TAG, "Invalid battery capacity: " + batteryCapacity);
            return 0.0f;
        }
        return (batteryLevel / batteryCapacity) * 100.0f;
    }

        // 배터리와 속도에 따른 주행 가능 거리 계산 함수
        private float updateRangeStatus(float batteryPercentage, float currentSpeed) {
            final float MAX_RANGE = 450.0f; // 100%일 때 최대 주행거리 (km)
            float speedFactor = calculateSpeedFactor(currentSpeed); // 속도에 따른 가중치 계산
            return (batteryPercentage / 100.0f) * MAX_RANGE * speedFactor;
        }
    
        // 속도에 따른 가중치 계산 함수
        private float calculateSpeedFactor(float speed) {
            return Math.max(0.1f, 1.0f - (speed * 0.0045f));
        }
    

    private void checkAndStopEvSafeApp() {
        if (!gearStatus.equals("P")) {
            boolean isEvSafeRunning = isAppRunning("com.example.evsafe");

            if (isEvSafeRunning) {
                Log.d(TAG, "checkAndStopEvSafeApp: EVSafe app is running. Attempting to stop it.");
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                PackageManager packageManager = getPackageManager();
                try {
                    ApplicationInfo appInfo = packageManager.getApplicationInfo("com.example.evsafe", 0);
                    activityManager.killBackgroundProcesses(appInfo.packageName);
                    Toast.makeText(this, "주행중에는 EVSafe 앱 사용이 제한됩니다.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "checkAndStopEvSafeApp: EVSafe app stopped successfully.");
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "checkAndStopEvSafeApp: EVSafe app not found", e);
                }
            } else {
            }
        } else {
        }
    }

    // NOT WORKING YET
    private boolean isAppRunning(String packageName) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - UPDATE_INTERVAL_MS;
    
        Log.d(TAG, "Checking app usage for package: " + packageName);
        Log.d(TAG, "Start time: " + startTime + ", End time: " + endTime);
    
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
    
        if (usageStatsList == null || usageStatsList.isEmpty()) {
            Log.d(TAG, "No usage stats found for the specified interval.");
            return false; // 결과 없음
        }
    
        SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
        for (UsageStats usageStats : usageStatsList) {
            Log.d(TAG, "UsageStats found: " + usageStats.getPackageName() + ", Last time used: " + usageStats.getLastTimeUsed());
            sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
        }
    
        if (!sortedMap.isEmpty()) {
            String lastUsedApp = sortedMap.get(sortedMap.lastKey()).getPackageName();
            Log.d(TAG, "Last used app: " + lastUsedApp);
    
            if (lastUsedApp.equals(packageName)) {
                Log.d(TAG, packageName + " is running (last used).");
                return true; // 앱이 최근에 사용됨
            } else {
                Log.d(TAG, packageName + " is not the last used app.");
            }
        } else {
            Log.d(TAG, "Sorted map is empty.");
        }
        return false; // 앱이 사용되지 않음
    }

    private void checkUsageStatsPermission(Context context) {
        if (!hasUsageStatsPermission(context)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
    
    private boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // 프로세스 중요도를 문자열로 변환하는 helper 메소드
    private String getImportanceString(int importance) {
        switch (importance) {
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
                return "FOREGROUND";
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE:
                return "VISIBLE";
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE:
                return "SERVICE";
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
                return "BACKGROUND";
            case ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY:
                return "EMPTY";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        isServiceRunning = false;
        handler.removeCallbacks(updateRunnable);
        if (car != null) {
            try {
                car.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error disconnecting car", e);
            }
            car = null;
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
