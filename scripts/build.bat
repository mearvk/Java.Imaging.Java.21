@echo off
REM Compile & Run script for Windows
REM Usage: scripts\build.bat

echo === Compiling Java.Imaging.Java.21 ===

if not exist out mkdir out

javac -sourcepath src -d out ^
  src\com\mearvk\imaging\ImageMetadataReader.java ^
  src\security\ExceptionHandler.java ^
  src\security\SecurityHandler.java ^
  src\security\CertificateHandler.java ^
  src\security\UsageHandler.java ^
  src\pennywise\XMLHandler.java ^
  src\pennywise\EntertainmentHandler.java ^
  src\pennywise\DatabaseHandler.java ^
  src\pennywise\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo === Build FAILED ===
    pause
    exit /b 1
)

echo === Build successful ===
echo.
echo Run with:
echo   java -cp out pennywise.Main src\pennywise\config.xml
