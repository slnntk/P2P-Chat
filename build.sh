#!/bin/bash

# P2P Chat Build Script

echo "Building P2P Chat System..."

# Create directories
mkdir -p build/classes

# Compile Java sources
javac -d build/classes src/main/java/com/p2pchat/*.java

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "Classes compiled to build/classes/"
    
    # Create a simple JAR file
    cd build/classes
    jar -cfe ../../p2pchat.jar com.p2pchat.ChatClient com/p2pchat/*.class
    cd ../..
    
    if [ -f "p2pchat.jar" ]; then
        echo "JAR file created: p2pchat.jar"
        echo ""
        echo "To run the application:"
        echo "  java -jar p2pchat.jar"
        echo "  OR"
        echo "  ./run.sh"
    fi
else
    echo "Build failed!"
    exit 1
fi