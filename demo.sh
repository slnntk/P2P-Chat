#!/bin/bash

# P2P Chat Demonstration Script
# This script demonstrates how to set up multiple peers for testing

echo "=== P2P Chat System Demonstration ==="
echo "This script will help you test the P2P chat with multiple peers."
echo ""

# Check if the JAR file exists
if [ ! -f "p2pchat.jar" ]; then
    echo "Building the project first..."
    ./build.sh
fi

echo "To test the P2P Chat system with multiple peers:"
echo ""
echo "1. Open multiple terminal windows"
echo "2. In each terminal, navigate to this directory"
echo "3. Run: ./run.sh"
echo "4. Set up peers as follows:"
echo ""
echo "   Terminal 1 (Peer Alice):"
echo "   - Username: Alice"
echo "   - Port: 8080"
echo ""
echo "   Terminal 2 (Peer Bob):"
echo "   - Username: Bob"
echo "   - Port: 8081"
echo "   - Connect to Alice: connect localhost:8080"
echo ""
echo "   Terminal 3 (Peer Charlie):"
echo "   - Username: Charlie"
echo "   - Port: 8082"
echo "   - Use 'discover' command to find and connect to peers"
echo ""
echo "5. Start chatting! Messages from any peer will be broadcasted to all connected peers."
echo ""
echo "Useful commands to try:"
echo "   status      - See connected peers"
echo "   history     - View message history"
echo "   discover    - Find peers automatically"
echo ""
echo "Press Ctrl+C in any terminal to gracefully exit that peer."
echo ""
echo "=== Starting Demo Instance ==="
echo "This will start a demo peer on port 8080..."
echo "You can connect to it from other terminals using: connect localhost:8080"
echo ""

# Start a demo instance
echo "DemoUser" | java -jar p2pchat.jar