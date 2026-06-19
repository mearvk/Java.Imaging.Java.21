#!/bin/bash
# Compile & Run script for Linux/macOS
# Usage: bash scripts/build.sh

set -e

echo "=== Compiling Java.Imaging.Java.21 ==="

mkdir -p out

javac -sourcepath src -d out \
  src/com/mearvk/imaging/ImageMetadataReader.java \
  src/security/ExceptionHandler.java \
  src/security/SecurityHandler.java \
  src/security/CertificateHandler.java \
  src/security/UsageHandler.java \
  src/pennywise/XMLHandler.java \
  src/pennywise/EntertainmentHandler.java \
  src/pennywise/DatabaseHandler.java \
  src/pennywise/Main.java

echo "=== Build successful ==="
echo ""
echo "Run with:"
echo "  java -cp out pennywise.Main src/pennywise/config.xml"
