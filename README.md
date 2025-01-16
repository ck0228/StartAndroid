# AOSP based Andorid Automotive OS

## Version 1 (✅ COMPLETED)
### STEP 1 (✅ COMPLETE)
#### EVSafe App Service
- [X] Gear Status Check and Display : P/R/N/D
- [X] EV Battery level Display
- [X] Current Speed Display
- [X] Available Distance Display
------------------------------------
### STEP 2 (✅ COMPLETE)
#### EVSafe App Service
- [X] Gear Status Check and Toast Meesage : if not P
- [X] Current Speed Check and Notification : if too fast

#### EvSafeService
- Gear Status Check and Toast Meesage : if not P
    - [X] Gear Status Check on System Service

-  Speed Check on System Service
    - [X] Current Speed Check on System Service
------------------------------------
### STEP 3 (🔥 WORKING ON)

- Speed Check on System Service
    - [X] Send Notification when Available Range is below 100km
------------------------------------

## Version 2 (🔥 WORKING ON)
### STEP 4 (✅ COMPLETED)
#### EvSafeService on Framework Level
- [X] Vehicle Status callbacks
- [X] Notification callbacks
- [X] Boot callbacks
- [X] Make Configs
- [X] Debug infinite Notification making issue
- [X] Deconnect ServiceNotification
- [X] Leave WarningNotification Only
------------------------------------
### STEP 5 (✅ COMPLETEDN)
- Interface with EvSafe App
    - [X] Send and Get Vehicle Status
    - [X] Update Status
    - [X] Handle EvSafe App
- [X] Modify BluePrint and Manifest files
------------------------------------
### STEP 6 (Additional)
- [ ] Study BluePrint and Manifest files
- [ ] Broadcast only when Statuses are changed in EvSafeService
- [ ] Broadcast statuses seperately



## Architecture -- need to be updated
| Layers                      | Components                                 |
|-------------------------------|-----------------------------------------|
| **EVSafe Application**         | updateVehicleStatus()<br> - Gear<br> - Battery<br> - Speed<br> - Range |
|                               |                                         |
| **API**                       | intent, binder, car api                    |
|                               |                                         |
| **EvSafeService**      | EvSafeService    |
|                               |                                         |
| **Android Native Service**     | CarPropertyManager                   |
|                               |                                         |
| **OS System Service**          | Notification, Toast Message, Service     |



# Env
- OS : linux
- Verion : android 12
- Branch : android-12.0.0_r13
- Lunch : sdk_car_x86_64-userdebug

# References
- https://developer.android.com/reference/android/car/VehiclePropertyIds?hl=en
- https://cs.android.com/android/platform/superproject/+/android-12.0.0_r14:
- Udemy: Android Open Source Project Development (AOSP) - Android Automotive
  https://www.udemy.com/course/android-os-internals-aosp-automotive-development
- Inside Android OS (book)

----
# ADBS
- 배터리 용량 설정 (kWh)
```
adb shell cmd car_service vehicle-hal-property-set --property 0x15450 --area 0x0 --float 75.0
```
- 현재 배터리 잔량 설정 (kWh)
```
adb shell cmd car_service vehicle-hal-property-set --property 0x15451 --area 0x0 --float 50.0
```
- 속도 설정 (km/h)
```
adb shell cmd car_service vehicle-hal-property-set --property 0x11600 --area 0x0 --float 60.0
```

- 기어 설정 
- GEAR_NEUTRAL = 1
- GEAR_REVERSE = 2
- GEAR_PARK = 4
- GEAR_DRIVE = 8
```
adb shell cmd car_service vehicle-hal-property-set --property 0x11400 --area 0x0 --int32 8
```
