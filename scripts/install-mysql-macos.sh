#!/bin/bash
# MySQL Install Script for macOS
# Run: bash scripts/install-mysql-macos.sh

set -e

echo "=== Installing MySQL on macOS ==="

# Install via Homebrew
if ! command -v brew &> /dev/null; then
    echo "Homebrew not found. Install from https://brew.sh first."
    exit 1
fi

brew install mysql

# Start service
brew services start mysql

# Wait for MySQL to start
sleep 3

# Create database and user
echo "Creating imaging database..."
mysql -u root -e "CREATE DATABASE IF NOT EXISTS imaging;"
mysql -u root -e "CREATE USER IF NOT EXISTS 'imaging'@'localhost' IDENTIFIED BY 'imaging2024';"
mysql -u root -e "GRANT ALL PRIVILEGES ON imaging.* TO 'imaging'@'localhost';"
mysql -u root -e "FLUSH PRIVILEGES;"

# Create tables
mysql -u root imaging -e "
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
