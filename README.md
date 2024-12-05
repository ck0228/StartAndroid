# AOSP based Andorid Automotive OS

## Key Features
### STEP 1 (✅ COMPLETE)
#### EVSafe App Service
- [X] Gear Status Check and Display : P/R/N/D
- [X] EV Battery level Display
- [X] Current Speed Display
- [X] Available Distance Display
------------------------------------
### STEP 2 (🔥 WORKING ON)
#### EVSafe App Service
- [ ] Gear Status Check and Toast Meesage : if not P
- [ ] Current Speed Check and Notification : if too fast

#### EvSafeService
- Gear Status Check and Toast Meesage : if not P
  - [ ] Gear Status Check on System Service

-  Speed Check on System Service
  - [ ] Current Speed Check on System Service
------------------------------------
### STEP 3 (TBU)
#### EvSafeService
-  Gear Status Check and Toast Meesage : if not P
  - [ ] Toast message on Specific App

- Speed Check on System Service
  - [ ] Send Notification if too fast
------------------------------------

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

