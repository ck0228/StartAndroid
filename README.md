# AOSP based Andorid Automotive OS

## Key Features
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
#### EvSafeService
-  Gear Status Check and Toast Meesage : if not P
    - [ ] Toast message on Specific App : EVSafe

- Speed Check on System Service
    - [X] Send Notification when Available Range is below 100km
------------------------------------

## Architecture
| Layers                      | Components                                 |
|-------------------------------|-----------------------------------------|
| **EVSafe Application**         | Notification, Toast, Views<br> - Gear, -Battery<br> - Speed, - Range |
|                               |                                         |
| **API**                       | updateVehicleStatus(), UpdateNotification() |
|                               |                                         |
| **Car API**                   | CarPropertyManager, VehiclePropertyIDs |
|                               |                                         |
| **Android Native Service**     | Car Service, Service, NotificationManager |
|                               |                                         |
| **HAL**                       | Vehicle HAL     |


## Sequence Diagram
![EvSafeService_SequenceDiagram](https://github.com/user-attachments/assets/434e65c8-2ff3-4272-b7db-cc6958234936)



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

