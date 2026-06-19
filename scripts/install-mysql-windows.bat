@echo off
REM MySQL Install Script for Windows
REM Run as Administrator: scripts\install-mysql-windows.bat
REM
REM This script downloads and installs MySQL via the MSI installer.

echo === Installing MySQL on Windows ===

REM Check if MySQL is already installed
where mysql >nul 2>nul
if %ERRORLEVEL%==0 (
    echo MySQL already installed.
    goto setup
)

echo.
echo Please download MySQL Community Server from:
echo   https://dev.mysql.com/downloads/mysql/
echo.
echo Install the MSI installer and ensure "mysql" is in your PATH.
echo After installation, re-run this script.
echo.
pause
goto end

:setup
echo Creating imaging database...

mysql -u root -e "CREATE DATABASE IF NOT EXISTS imaging;"
mysql -u root -e "CREATE USER IF NOT EXISTS 'imaging'@'localhost' IDENTIFIED BY 'imaging2024';"
mysql -u root -e "GRANT ALL PRIVILEGES ON imaging.* TO 'imaging'@'localhost';"
mysql -u root -e "FLUSH PRIVILEGES;"

mysql -u root imaging -e "CREATE TABLE IF NOT EXISTS file_log (id INT AUTO_INCREMENT PRIMARY KEY, original_name VARCHAR(512), new_name VARCHAR(512), file_type VARCHAR(10), date_taken DATETIME, source_path VARCHAR(1024), dest_path VARCHAR(1024), processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"

mysql -u root imaging -e "CREATE TABLE IF NOT EXISTS usage_log (id INT AUTO_INCREMENT PRIMARY KEY, run_count INT, run_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"

echo === MySQL imaging database ready ===
echo Connection: jdbc:mysql://localhost:3306/imaging
echo User: imaging / imaging2024

:end
