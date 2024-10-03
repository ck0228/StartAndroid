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
