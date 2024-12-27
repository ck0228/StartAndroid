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
### STEP 4 (🔥 WORKING ON)
#### EvSafeService on Framework Level
- [X] Vehicle Status callbacks
- [X] Notification callbacks
- [X] Boot callbacks
- [X] Make Configs
- [ ] Modify BluePrint and Manifest files(Still Studying)
- [X] Debug infinite Notification making issue
- [X] Deconnect ServiceNotification
- [X] Leave WarningNotification Only
------------------------------------
### STEP 5 ()
- Interface with EvSafe App
    - [ ] Send and Get Vehicle Status
    - [ ] Update Status
    - [ ] Handle EvSafe App








## Architecture
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

