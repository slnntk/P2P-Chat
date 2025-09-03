#!/bin/bash

# P2P Chat Run Script

if [ ! -f "p2pchat.jar" ]; then
    echo "JAR file not found. Building project..."
    ./build.sh
fi

if [ -f "p2pchat.jar" ]; then
    echo "Starting P2P Chat..."
    java -jar p2pchat.jar
else
    echo "Error: Cannot find or create p2pchat.jar"
    echo "Try running ./build.sh first"
    exit 1
fi