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
   wsl --install -d Ubuntu20.04
   ```

#### 1-4. WSL 버전 확인
다음 명령어 실행:
```
wsl -l -v
```

결과 예시:
![WSL 버전 확인](https://github.com/user-attachments/assets/313cd0a1-84b8-47ff-b37b-70439624b03f)

#### 1-5. wsl limit 해제
.wslconfig 파일 생성 또는 편집: Windows에서 다음 경로에 .wslconfig 파일을 생성하거나 편집합니다:

C:\Users\<YourUserName>\.wslconfig
.wslconfig 파일에 아래 내용 입력

[wsl2]
memory=16GB
processors=4
swap=8GB
localhostForwarding=true
변경사항 적용:

```
wsl --shutdown
```


### 3. AOSP

#### 3-1. 필수패키지 다운로드
on ubuntu
```
sudo apt-get update
sudo apt-get install git-core gnupg flex bison build-essential zip curl zlib1g-dev libc6-dev-i386 lib32ncurses5-dev x11proto-core-dev libx11-dev lib32z1-dev libgl1-mesa-dev libxml2-utils xsltproc unzip fontconfig zip libncurses5
sudo apt-get repo
```

#### 3-2. Source Code download
it takes time
```
mkdir ~/android-source
PATH=~/android-source:$PATH
cd ~/android-source
curl https://storage.googleapis.com/git-repo-downloads/repo > repo
chmod a+x repo
```
path 설정
```
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
export PATH="/usr/bin:$PATH"
export PYTHON_CONFIGURE_OPTS="--enable-unicode=ucs4"
export PATH=~/bin:$PATH
```

```
git config --global user.name "Chan Kim"
git config --global user.email "chansk0228@gmail.com"
sudo apt-get install python-is-python3

repo init -u https://android.googlesource.com/platform/manifest -b android-8.0.0_r1
repo sync -c -j4
```

if somthing is wrong do this
```
repo sync
```

#### 3-5. build sourcecode in to docker
빌드
```
cd ~/android-source
source build/envsetup.sh
lunch
6
m -j$(nproc)
```
