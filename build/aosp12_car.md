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

emulator 문제 발생 유형
- 이도저도 안되면 emulator 삭제 후 rebuild
```
rm -rf ~/.android/avd/*
cd ~/aaos
source build/envsetup.sh
lunch sdk_car_x86_64-userdebug
m clean
m -j12
```
- 데이터 초기화
```
emulator -wipe-data
```
- phone is starting에서 안넘어감
```
emulator -netspeed full -netdelay none
```
