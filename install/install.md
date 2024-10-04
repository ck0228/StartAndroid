## Install

### 1. WSL

#### 1-1. Linux activate
관리자 권한으로 Windows PowerShell 실행 후 명령어 입력

Windows SubSystem Linux를 활성화시키는 명령어:
```
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
```

VirtualMachinePlatform 기능을 활성화시키는 명령어 (WSL2 버전에 필요):
```
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
```

#### 1-2. 컴퓨터 재부팅
재부팅 후 `wsl` 명령어 실행하여 다음과 같이 출력되는지 확인:

![WSL 확인](https://github.com/user-attachments/assets/73e7fc10-65c7-4f35-a4ea-b6d89446abe2)

#### 1-3. WSL2 Linux 커널 업데이트
1. [WSL2 Linux kernel update package](https://aka.ms/wsl2kernel) 다운로드
2. 다운로드한 파일 실행하여 설치
3. PowerShell(관리자 권한)에서 WSL2를 기본 버전으로 설정:
   ```
   wsl --set-default-version 2
   ```
4. Ubuntu 설치:
   ```
   wsl --install -d Ubuntu
   ```

#### 1-4. WSL 버전 확인
다음 명령어 실행:
```
wsl -l -v
```

결과 예시:
![WSL 버전 확인](https://github.com/user-attachments/assets/313cd0a1-84b8-47ff-b37b-70439624b03f)

### 3. AOSP

#### 3-1. 필수패키지 다운로드
on ubuntu
```
sudo apt update
sudo apt install git-core gnupg flex bison build-essential zip curl zlib1g-dev gcc-multilib g++-multilib libc6-dev-i386 lib32ncurses5-dev x11proto-core-dev libx11-dev lib32z1-dev libgl1-mesa-dev libxml2-utils xsltproc unzip fontconfig
sudo apt-get install openjdk-8-jre
```
```
apt install repo
```

#### 3-2. Source Code download
it takes time
```
mkdir ~/android-source
cd ~/android-source
repo init -u https://android.googlesource.com/platform/manifest -b android-8.0.0_r1
repo sync
```
if somthing is wrong do this again
```
repo sync
```

#### 3-5. build sourcecode in to docker
빌드
```
cd ~/android-source
source build/envsetup.sh
lunch aosp_arm-eng
m -j$(nproc)
```
