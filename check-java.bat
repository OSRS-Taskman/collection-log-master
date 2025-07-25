@echo off

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 echo Java found!
if not %ERRORLEVEL% equ 0 echo Java NOT found!
pause