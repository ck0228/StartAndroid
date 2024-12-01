# AOSP based Andorid Automotive OS

## Key Features
### STEP 1 (✅ COMPLETE)
#### EVSafe App Service
- [X] Gear Status Check and Display : P/R/N/D
- [X] EV Battery level Display
- [X] Current Speed Display
- [ ] Available Distance Display
------------------------------------
### STEP 2 (🔥 WORKING ON)
#### EVSafe System Service
- [ ] Disable Game App on Gear D
  - [ ] 1) Gear Status Check on System Service
  - [ ] 2) Disable Game App


- [ ] EV Battery Level Check on System Service
  - [ ] 1) Input Battery Level
  - [ ] 2) EV Battery level Check on System Service
------------------------------------
### STEP 3 (TBU)
#### EVSafe System Service + System Alarm
- [ ] Disable Game App on Gear D **With Toast Message**
- [ ] Low Battery **Notification**
------------------------------------

## Architecture
| Layers                      | Components                                 |
|-------------------------------|-----------------------------------------|
| **EvSafe Application**         | updateVehicleStatus()<br> - Gear<br> - Battery<br> - Speed |
|                               |                                         |
| **API**                       | stub, binder, aidl                    |
|                               |                                         |
| **EvSafe System Service**      | EvsBatteryManager, EvsGearManager    |
|                               |                                         |
| **Android System Service**     | CarPropertyManager                   |
|                               |                                         |
| **OS System Service**          | Notification, Toast Message         |



# Env
- OS : linux
- Verion : android 12
- Branch : android-12.0.0_r13
- Lunch : sdk_car_x86_64-userdebug

# References
- https://developer.android.com/reference/android/car/VehiclePropertyIds?hl=en
- https://cs.android.com/android/platform/superproject/+/android-12.0.0_r14:
