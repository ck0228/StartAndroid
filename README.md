# StartAndroid
Studying Inside Android OS

# Install

1. WSL

   1-1. Linux activate
   관리자 권한으로 Windows PowerShell 실행 후 명령어 입력

# Windows SubSystem Linux를 활성화시키는 명령어
> dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart

# VirtualMachinePlatform 기능을 활성화시키는 명령어 : WSL2 버전에 필요한 명령어
> dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart


  1-2. 컴퓨터 재부팅 한 다음 wsl 명령어 실행한 뒤 다음과 같이 출력되는지 확인
  ![image](https://github.com/user-attachments/assets/73e7fc10-65c7-4f35-a4ea-b6d89446abe2)


  1-3.WSL2 Linux 커널 업데이트 패키지 다운로드:
  https://aka.ms/wsl2kernel 링크로 이동하여 "WSL2 Linux kernel update package for x64 machines"를 다운로드하세요.
  다운로드한 파일을 실행하여 설치합니다.
  설치가 완료되면 PowerShell이나 명령 프롬프트를 관리자 권한으로 실행하고 다음 명령어를 입력하여 WSL2를 기본 버전으로 설정합니다:
> wsl --set-default-version 2
  
  이제 다시 Ubuntu를 설치해 보세요:
> wsl --install -d Ubuntu


  1-4.wsl -l -v를 입력
  ![image](https://github.com/user-attachments/assets/313cd0a1-84b8-47ff-b37b-70439624b03f)


2. Docker

   2-1.Docker Desktop for Windows 설치
   하단의 페이지로 이동해서  Docker Desktop for Windows를 다운로드 받습니다. 
https://docs.docker.com/desktop/install/windows-install/


  2-2. Docker Desktop 앱 실행
