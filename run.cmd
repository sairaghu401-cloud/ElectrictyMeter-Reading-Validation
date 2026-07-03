@echo off
REM Build and run PS-52 Electricity Meter-Reading Validation (terminal app).
setlocal
cd /d "%~dp0"

if not exist out mkdir out

echo Compiling...
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
set RC=%errorlevel%
del sources.txt
if not "%RC%"=="0" (
    echo Compilation failed.
    exit /b %RC%
)

echo Starting app...
echo.
java -cp out com.utility.meter.app.Main
endlocal
