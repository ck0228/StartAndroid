# EvSafe Source Code

## 1. legacy -- deprecated
- framework: not working
- package: app + service + broadcasting
- package_v2: app + service + broadcasting refactored
- package_v3: app not working

## 2. packages: code for App / System App
- App: EVSafe
- Service: EvSafeService
- IPC + Binder

## 3. EvSafeService Source code
- Service  
path: packages/services/Car/service/  
  ㄴ Android.bp  
  ㄴ AndroidManifest.xml  
  ㄴ src/com/android/car/evsafeservice/  
  | ㄴ **EvSafeService.java**  
  | ㄴ **EvSafeServiceConfigs.java**  
  | ㄴ **EvSafeServiceData.java**  
  | ㄴ **EvSafeServiceDataManager.java**  
  | ㄴ **EvSafeServiceNotificationHelper.java**  
  | ㄴ **BootCompleteReceiver.java**

- API  
path: packages/services/Car/car-lib/  
  ㄴ Android.bp  
  ㄴ src/android/car/evsafeservice/  
  | ㄴ **IEvSafeService.aidl**  
  | ㄴ **IEvSafeServiceCallback.aidl**  

- App  
path: packages/apps/Car/EVSafe/  
  ㄴ Android.bp  
  ㄴ AndroidManifest.xml  
  ㄴ res/  
  ㄴ src/main/java/com/example/evsafe/  
  | ㄴ **MainActivity.java**  

### 1) Service OverView  
#### EvSafeService.java
- Initialization
- Service Life Cycle
- IPC with Car Service
- Register Listeners and Callbacks
- Invoke Callbacks
- Make Warning Notification

#### EvSafeServiceConfigs.java
- Manage Constants
- Thresholds, Notification Channel, Default Values

#### EvSafeServiceData.java
- Data: Gear, Speed, Battery, Range, Warning Flag
- Getters & Setters
- Values Compare

#### EvSafeServiceDataManager.java
- Register Listeners & Event Callback from CarPropertyManager
- Convert Properties to User Friendly Units(km/h, %, PRND)
- Update Properties on EvSafeServiceData

#### EvSafeServiceNotificationHelper.java
- Create and Show Notification

#### BootCompleteReceiver.java
- Receive Boot Complete
- Start EvSafeService

### 2) API Overview
#### IEvSafeService.aidl
- register & unregister callbacks

#### IEvSafeServiceCallback.aidl
- Send Data: Gear, Speed, Battery, Range

### 3) App Overview
#### MainActivity.java
- Initialize UI
- Connect EvSafeService & Register Callbacks
- Bind Service
- Update Components
