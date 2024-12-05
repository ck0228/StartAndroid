# EvSafeService

## BUILD
```
source build/envsetup.sh
lunch sdk_car_x86_64-userdebug
mmm packages/apps/EvSafeService
```

## RUN
it will run on boot

but if you still need somehow
```
adb shell am start-foreground-service -n com.android.car.evsafeservice/.EvSafeService
```

## STOP
```
adb shell am force-stop com.android.car.evsafeservice
```
