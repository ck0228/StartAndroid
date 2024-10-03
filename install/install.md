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

### 2. Docker

#### 2-1. Docker Desktop for Windows 설치
[Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/) 다운로드 및 설치

#### 2-2. Docker Desktop 앱 설정
![Docker Desktop 설정](https://github.com/user-attachments/assets/efe2ead0-6001-4a8f-bcd8-36645717b6ed)

#### 2-3. Docker 실행 확인
```
wsl -l -v
docker version
```
![image](https://github.com/user-attachments/assets/da694025-3fa3-42a0-a710-d41f720efc21)

### 3. AOSP

#### 3-1. 필수패키지 다운로드
on ubuntu
```
sudo apt update
sudo apt install git-core gnupg flex bison build-essential zip curl zlib1g-dev gcc-multilib g++-multilib libc6-dev-i386 lib32ncurses5-dev x11proto-core-dev libx11-dev lib32z1-dev libgl1-mesa-dev libxml2-utils xsltproc unzip fontconfig
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

#### 3-1. dockerfile
https://android.googlesource.com/platform/build/+/main/tools/docker
에서 tgz 다운 후 압축풀기

```
mkdir -p ~/android-source/build/tools/docker
cd ~/android-source/build/tools/docker
curl -o Dockerfile.encoded https://android.googlesource.com/platform/build/+/main/tools/docker/Dockerfile?format=TEXT
base64 -d Dockerfile.encoded > Dockerfile
rm Dockerfile.encoded
```

#### 3-2. gitconfig
if you already have gitconfig
```
cp ~/.gitconfig gitconfig
```

else start wsl and type this down
```
touch gitconfig
echo "[user]
   name = Your Name
   email = your.email@example.com" > gitconfig
```

#### 3-3. Dockerfile Edit
##### 3-3-1. checksum update
최신 repo 파일 다운로드:
WSL이나 Linux 환경에서 다음 명령어를 실행합니다:
```
curl -o repo https://storage.googleapis.com/git-repo-downloads/repo
```
다운로드한 파일의 SHA256 체크섬 계산:
```
sha256sum repo
```
이 명령어는 다음과 유사한 출력을 생성합니다:
```
a123b456c789d012e345f678g901h234i567j890k123l456m789n012o345p  repo
```
Dockerfile 내용 업데이트:
계산된 새로운 체크섬을 사용하여 Dockerfile의 해당 라인을 업데이트합니다. 예를 들어:
Dockerfile RUN curl -o /usr/local/bin/repo https://storage.googleapis.com/git-repo-downloads/repo \
 && echo "a123b456c789d012e345f678g901h234i567j890k123l456m789n012o345p  /usr/local/bin/repo" | sha256sum --strict -c - \
 && chmod a+x /usr/local/bin/repo
여기서 a123b456c789d012e345f678g901h234i567j890k123l456m789n012o345p는 실제로 계산된 체크섬으로 교체해야 합니다.

##### 3-3-2. user update
```
RUN groupadd -g $groupid $username \
 && useradd -m -u $userid -g $groupid $username \
 && echo $username >/root/username \
 && echo "export USER="$username >>/home/$username/.gitconfig
COPY gitconfig /home/$username/.gitconfig
RUN chown $userid:$groupid /home/$username/.gitconfig

ENV HOME=/home/$username
ENV USER=$username

ENTRYPOINT ["chroot", "--userspec=$(cat /root/username):$(cat /root/username)", "/", "/bin/bash", "-i"]
```
위 부분을 지우고 아래와 같이 수정
```
RUN if [ "$username" != "root" ]; then \
      groupadd -g $groupid $username && \
      useradd -m -u $userid -g $groupid $username && \
      echo $username >/root/username && \
      echo "export USER=$username" >>/home/$username/.gitconfig; \
    else \
      echo $username >/root/username && \
      echo "export USER=$username" >>/root/.gitconfig; \
    fi
                                                                                                                        RUN if [ "$username" != "root" ]; then \
      echo "[user]\n    name = Android Builder\n    email = android@example.com\n[color]\n    ui = auto" > /home/$usern>      chown $userid:$groupid /home/$username/.gitconfig; \
    else \
      echo "[user]\n    name = Android Builder\n    email = android@example.com\n[color]\n    ui = auto" > /root/.gitco>    fi

ENV HOME=/home/$username
ENV USER=$username

ENTRYPOINT ["/bin/bash", "-i"]
```

#### 3-4. Build Container
```
cd ~/android-source
docker build --build-arg userid=$(id -u) --build-arg groupid=$(id -g) --build-arg username=$(id -un) -t android-build-trusty build/tools/docker
```
Then you can start up new instances with:
```
docker run -it --rm -v $(pwd):/src android-build-trusty
```
#### 3-5. get in to docker
container 내부에서 사용자 생성 및 소유권 변경
```
adduser android
usermod -aG sudo android
chown -R android:android /src
chown -R android:android /src/out
chmod -R 755 /src/out
export USER=android
```
빌드
```
su - android
cd /src
source build/envsetup.sh
lunch aosp_arm-eng
m -j4
```
