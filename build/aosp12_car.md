build
```
cd ~/aaos
source build/envsetup.sh
lunch
sdk_car_x86_64-userdebugm
```

open CarService with vscode
```
aidegen -i vscode CarService
```

adb 연결 및 emulator 실행
```
cd ~/aaos
source build/envsetup.sh
lunch sdk_car_x86_64-userdebug

# 기존 ADB 서버 중지
adb kill-server

# 새로운 ADB 서버 시작
sudo adb start-server

emulator
```

emulator 문제 발생시
1. emulator 삭제
```
rm -rf ~/.android/avd/<에뮬레이터_이름>.avd
rm -rf ~/.android/avd/<에뮬레이터_이름>.ini
```
2. rebuild
```
cd ~/aaos
source build/envsetup.sh
lunch sdk_car_x86_64-userdebug
```
