# 빌드
build
```
cd ~/aaos
source build/envsetup.sh
lunch sdk_car_x86_64-userdebug
m
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

# 기존 ADB 서버 중지(기존 서버 실행중일시)
adb kill-server

# 새로운 ADB 서버 시작
sudo adb start-server

emulator
```

# emulator 실행 관련 문제해결

- (최악, 비추)이도저도 안되면 emulator 삭제 후 rebuild
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
[네트워크 문제 확인]
```
adb shell
ifcofig
```
1. **인터페이스 확인**:
   - `eth0` 또는 `wlan0` 인터페이스가 존재해야 합니다.

2. **IP 주소 할당 확인**:
   - 각 인터페이스에 **IP 주소**가 할당되어 있는지 확인합니다.
   - IP 주소가 없다면 **DHCP 설정 문제**일 수 있습니다.

   예시:

   eth0      Link encap:UNSPEC    Driver virtio_net
             inet addr:10.0.2.15   Bcast:10.255.255.255  Mask:255.0.0.0


위와 같은 형식의 출력에서 `inet addr` 부분이 확인되지 않는다면, 네트워크 문제가 있을 수 있습니다.


[에뮬레이터 네트워크 설정 초기화]
```
adb emu kill
emulator -netspeed full -netdelay none
```

[eth0 강제 비활성화/활성화]
```
adb root
adb shell ifconfig eth0 down
adb shell ifconfig eth0 up
```

[서버 재부팅]
```
adb reboot
```

이건 검증중
```
adb shell ip route flush table main
adb shell svc wifi disable
adb shell svc wifi enable
adb shell ip route add default via 10.0.2.2
```
