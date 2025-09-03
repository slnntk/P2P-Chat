# P2P Chat System

A decentralized peer-to-peer chat system that allows multiple users to communicate without requiring a central server. Built in Java using TCP/IP sockets.

## Features

- **Multiple Peer Connections**: Connect to multiple peers simultaneously
- **User Identification**: Display sender names with each message
- **Message Broadcasting**: Efficient message distribution to all connected peers
- **Message History**: Session-based message logging and display
- **Peer Discovery**: Automatic discovery of peers on the local network
- **Graceful Shutdown**: Proper connection cleanup when exiting
- **Console Interface**: Easy-to-use command-line interface

## Architecture

The system follows a decentralized P2P architecture where each peer acts as both a client and server:

- **Peer**: Main class managing multiple connections and message broadcasting
- **PeerConnection**: Handles individual peer-to-peer connections
- **Message**: Represents chat messages with sender information and timestamps
- **MessageHistory**: Thread-safe message logging and retrieval
- **PeerDiscovery**: Automatic peer discovery mechanism
- **ChatClient**: Console-based user interface

## Requirements

- Java 8 or higher
- Network connectivity (local network or localhost for testing)

## Quick Start

### Building the Project

```bash
# Make scripts executable (if needed)
chmod +x build.sh run.sh

# Build the project
./build.sh
```

### Running the Application

```bash
# Option 1: Using the run script
./run.sh

# Option 2: Direct JAR execution
java -jar p2pchat.jar

# Option 3: Using compiled classes
java -cp build/classes com.p2pchat.ChatClient
```

### Basic Usage

1. **Start the application** and enter your username
2. **Choose a port** (8080-8090) for listening to incoming connections
3. **Connect to peers** using one of these methods:
   - Use `discover` command to automatically find local peers
   - Use `connect <host:port>` to manually connect to a peer
4. **Start chatting** by typing messages and pressing Enter

## Commands

| Command | Description |
|---------|-------------|
| `connect <host:port>` | Connect to a specific peer (e.g., `connect localhost:8081`) |
| `discover` | Automatically discover and connect to local peers |
| `status` | Show current peer status and active connections |
| `history [count]` | Display message history (default: 10 messages) |
| `clear` | Clear the console screen |
| `help` | Show available commands |
| `quit` | Exit the application |

## Testing with Multiple Peers

To test the P2P chat system:

1. **Open multiple terminal windows**
2. **Start the first peer** on port 8080
3. **Start the second peer** on port 8081
4. **In the second peer**, use `connect localhost:8080` or `discover`
5. **Start additional peers** on different ports (8082, 8083, etc.)
6. **Connect peers** to create a network topology
7. **Send messages** from any peer to see them broadcasted to all connected peers

## Network Protocol

The system uses a simple text-based protocol over TCP:

- `HANDSHAKE:<username>` - Initial handshake with peer identification
- `MESSAGE:<sender>|<content>|<timestamp>` - Chat message format

## Technical Details

### Threading Model
- **Server Thread**: Accepts incoming peer connections
- **Connection Threads**: Handle individual peer communication
- **Main Thread**: Manages user interface and command processing

### Message Broadcasting
- Messages are broadcasted to all active peer connections
- Loop prevention: messages are not forwarded back to the sender
- Thread-safe message handling using concurrent collections

### Peer Discovery
- **Local Discovery**: Scans common ports (8080-8090) on localhost
- **Network Discovery**: Optionally scans the local subnet for peers
- **Timeout-based**: Uses connection timeouts to quickly identify active peers

## Limitations

- **Local Network**: Designed primarily for local network communication
- **No Encryption**: Messages are sent in plain text
- **No Persistence**: Message history is lost when the application closes
- **Simple Authentication**: Only username-based identification

## Future Enhancements

Potential improvements for the system:
- GUI interface using JavaFX or Swing
- Message encryption and security
- Persistent message storage
- User authentication and access control
- File sharing capabilities
- Network topology visualization
- NAT traversal for internet-wide P2P communication

## Troubleshooting

### Common Issues

1. **Port Already in Use**: Try a different port (8080-8090)
2. **Connection Refused**: Ensure the target peer is running and listening
3. **Firewall Issues**: Check firewall settings for the chosen ports
4. **No Peers Found**: Ensure other peers are running on common ports

### Debugging

- Use the `status` command to check active connections
- Check console output for connection/disconnection messages
- Verify network connectivity using standard tools (ping, telnet)

## License

This project is open source and available under the MIT License.