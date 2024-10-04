# AVD

## 1. Android SDK Manager
### 1-1. on wsl get android sdk manager
```
sudo apt update
sudo apt install openjdk-8-jdk wget unzip
sudo apt-get install -y wget
sudo wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip
```
### 1-2. install sdk manager
```
unzip commandlinetools-linux-8512546_latest.zip -d ~/android-sdk
mkdir -p ~/android-sdk/cmdline-tools/latest
mv ~/android-sdk/cmdline-tools/* ~/android-sdk/cmdline-tools/latest/
```
### 1-3. set env var
```
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
source ~/.bashrc
```
### 1-4. check sdkmanager
```
sdkmanager --version
```
### 1-5. download packages using sdkmanager
```
sdkmanager "platform-tools" "platforms;android-27" "build-tools;27.0.3" "emulator" "system-images;android-27;google_apis;x86"
```
### 1-6. create avd
```
avdmanager create avd -n aosp_avd -k "system-images;android-27;google_apis;x86"
```
생성확인
```
emulator -list-avds
```
aosp_avd 가 떠야함

### 1-7. Copy Image from docker
<docker_id>에 docker id 입력
<target_path>에 path 입력
  
```
docker cp <docker_id>:/src/out/target/product/generic/system.img <target_path>
docker cp <docker_id>:/src/out/target/product/generic/userdata.img <target_path>
docker cp <docker_id>:/src/out/target/product/generic/ramdisk.img <target_path>
```
evade encryption
```
touch <target_path>/encryptionkey.img
```

### 1-8. Emulator 실행

```
cd ~/.android/avd/aosp_avd.avd/
vi hardware-qemu.ini
```
hw.gps 를 true에서 false로 수정하여 gps기능 끄기
hw.gps = false


```
./emulator -avd aosp_avd -verbose -show-kernel -system /mnt/c/Android/system.img -data /mnt/c/Android/userdata.img -ramdisk /mnt/c/Android/ramdisk.img -no-snapshot -no-window -debug all -encryption-key /mnt/c/Android/encryptionkey.img
```
##
