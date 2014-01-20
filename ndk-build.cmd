@echo off
rem This is a Windows cmd.exe script used to invoke the NDK-specific GNU Make executable
set NDK_ROOT=D:\Android\android-ndk-r8\
set NDK_MAKE=%NDK_ROOT%/prebuilt/windows/bin/make.exe
%NDK_ROOT%\prebuilt\windows\bin\make.exe -f %NDK_ROOT%build/core/build-local.mk SHELL=cmd %* -j 4|| exit /b %ERRORLEVEL% 