# AOSP based Andorid Automotive OS

## Key Features
1. Gear Status Check and Display : P/R/N/D
2. EV Battery level Display
3. Speed Display
4. Available Distance Display
5. Lock Game app while driving - Toast message
6. EV Battery Low Alarm - Notification
7. EV Battery Level Change for test

## Architecture
+-------------------------+
|     EvSafe Application   |
|    (EvsSpeedManager)    |
+------------+------------+
             |
             |
+------------v------------+
|           API           |
|  (stub, binder, api)    |
+------------+------------+
             |
             |
+------------v------------+
|    EvSafe System Service  |
| (EvsBatteryManager, EvsGearManager) |
+------------+------------+
             |
             |
+------------v------------+
|   Android System Service  |
|    (CarPropertyManager)   |
+------------+------------+
             |
             |
+------------v------------+
|    OS System Service      |
|  (Notification, Toast Message) |
+-------------------------+

# Env
- OS : linux
- Verion : android 12
- Branch : android-12.0.0_r13
- Lunch : sdk_car_x86_64-userdebug
