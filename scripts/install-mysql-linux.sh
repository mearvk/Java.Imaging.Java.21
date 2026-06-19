#!/bin/bash
# MySQL Install Script for Linux (Ubuntu/Debian)
# Run: sudo bash scripts/install-mysql-linux.sh

set -e

echo "=== Installing MySQL on Linux ==="

# Update and install
sudo apt-get update
sudo apt-get install -y mysql-server mysql-client

# Start service
sudo systemctl start mysql
sudo systemctl enable mysql

# Create database and user for the imaging program
echo "Creating imaging database..."
sudo mysql -e "CREATE DATABASE IF NOT EXISTS imaging;"
sudo mysql -e "CREATE USER IF NOT EXISTS 'imaging'@'localhost' IDENTIFIED BY 'imaging2024';"
sudo mysql -e "GRANT ALL PRIVILEGES ON imaging.* TO 'imaging'@'localhost';"
sudo mysql -e "FLUSH PRIVILEGES;"

# Create tables
sudo mysql imaging -e "
CREATE TABLE IF NOT EXISTS file_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(512),
    new_name VARCHAR(512),
    file_type VARCHAR(10),
    date_taken DATETIME,
    source_path VARCHAR(1024),
    dest_path VARCHAR(1024),
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS usage_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    run_count INT,
    run_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
"

echo "=== MySQL installed and imaging database ready ==="
echo "Connection: jdbc:mysql://localhost:3306/imaging"
echo "User: imaging / imaging2024"
