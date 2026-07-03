@echo off
REM Build and run PS-52 Electricity Meter-Reading Validation (terminal app).
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo.
    echo ERROR: "java" was not found on PATH.
    echo Install a JDK, Java 17 or newer, from https://adoptium.net and try again.
    echo.
    pause
    exit /b 1
)

where javac >nul 2>nul
if errorlevel 1 (
    echo.
    echo ERROR: "javac" was not found on PATH.
    echo You have a JRE but not a JDK. Install a full JDK, Java 17 or newer, from https://adoptium.net
    echo.
    pause
    exit /b 1
)

if not exist out mkdir out

echo Compiling...
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
set RC=%errorlevel%
del sources.txt
if not "%RC%"=="0" (
    echo.
    echo Compilation failed. See errors above.
    echo.
    pause
    exit /b %RC%
)

echo Starting app...
echo.
java -cp out com.utility.meter.app.Main

echo.
echo App closed.
pause
endlocal
