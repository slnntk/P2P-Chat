#!/bin/bash

# P2P Chat GUI Runner Script

echo "Starting P2P Chat GUI..."

if [ -f "p2pchat-gui.jar" ]; then
    java -jar p2pchat-gui.jar
else
    echo "GUI JAR not found. Building first..."
    ./build-gui.sh
    
    if [ -f "p2pchat-gui.jar" ]; then
        java -jar p2pchat-gui.jar
    else
        echo "Build failed. Cannot start GUI."
        exit 1
    fi
fi