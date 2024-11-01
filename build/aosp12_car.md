build
```
cd ~/aosp12
source build/envsetup.sh
lunch
sdk_car_x86_64-userdebugm
```

open CarService with vscode
```
aidegen -i vscode CarService
```

emulator
```
cd ~/aosp12
source build/envsetup.sh
lunch sdk_car_x86_64-userdebug

# 기존 ADB 서버 중지
adb kill-server

# 새로운 ADB 서버 시작
sudo adb start-server

emulator
```
