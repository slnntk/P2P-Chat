#!/bin/bash

# P2P Chat Build Script with GUI Support

echo "Building P2P Chat System with GUI..."

# Create directories
mkdir -p build/classes

# Compile Java sources (base classes first, then GUI)
echo "Compiling base P2P classes..."
javac -d build/classes src/main/java/com/p2pchat/*.java

if [ $? -eq 0 ]; then
    echo "Compiling GUI classes..."
    javac -cp build/classes -d build/classes src/main/java/com/p2pchat/gui/*.java
fi

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "Classes compiled to build/classes/"
    
    # Create JAR file for console version
    cd build/classes
    jar -cfe ../../p2pchat-console.jar com.p2pchat.ChatClient com/p2pchat/*.class
    
    # Create JAR file for GUI version
    jar -cfe ../../p2pchat-gui.jar com.p2pchat.gui.ChatGUI com/p2pchat/*.class com/p2pchat/gui/*.class
    cd ../..
    
    if [ -f "p2pchat-console.jar" ] && [ -f "p2pchat-gui.jar" ]; then
        echo "JAR files created:"
        echo "  - p2pchat-console.jar (Console interface)"
        echo "  - p2pchat-gui.jar (Beautiful GUI interface)"
        echo ""
        echo "To run the applications:"
        echo "  Console version: java -jar p2pchat-console.jar"
        echo "  GUI version:     java -jar p2pchat-gui.jar"
        echo "  OR use ./run-gui.sh for the GUI version"
    fi
else
    echo "Build failed!"
    exit 1
fi