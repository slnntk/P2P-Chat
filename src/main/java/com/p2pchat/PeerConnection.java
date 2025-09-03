package com.p2pchat;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a connection to another peer in the P2P network.
 * Handles sending/receiving messages to/from a specific peer.
 */
public class PeerConnection {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final String remoteAddress;
    private final int remotePort;
    private final AtomicBoolean active;
    private final Peer parentPeer;
    private String remotePeerName;
    
    public PeerConnection(Socket socket, Peer parentPeer) throws IOException {
        this.socket = socket;
        this.parentPeer = parentPeer;
        this.remoteAddress = socket.getInetAddress().getHostAddress();
        this.remotePort = socket.getPort();
        this.active = new AtomicBoolean(true);
        
        // Initialize I/O streams
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        
        // Start listening for messages in a separate thread
        startListening();
    }
    
    /**
     * Starts listening for incoming messages from this peer
     */
    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                String line;
                while (active.get() && (line = reader.readLine()) != null) {
                    handleIncomingMessage(line);
                }
            } catch (IOException e) {
                if (active.get()) {
                    System.err.println("Error reading from peer " + getRemoteIdentifier() + ": " + e.getMessage());
                }
            } finally {
                close();
            }
        });
        
        listenerThread.setName("PeerConnection-" + getRemoteIdentifier());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    /**
     * Handles incoming messages from this peer
     */
    private void handleIncomingMessage(String rawMessage) {
        try {
            if (rawMessage.startsWith("HANDSHAKE:")) {
                // Handle handshake message to get peer name
                remotePeerName = rawMessage.substring(10);
                System.out.println("Peer " + remotePeerName + " (" + getRemoteIdentifier() + ") connected");
                return;
            }
            
            if (rawMessage.startsWith("MESSAGE:")) {
                // Handle regular message
                String messageData = rawMessage.substring(8);
                Message message = Message.deserialize(messageData);
                
                // Add to message history and notify parent peer
                parentPeer.receiveMessage(message, this);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing message from " + getRemoteIdentifier() + ": " + e.getMessage());
        }
    }
    
    /**
     * Sends a message to this peer
     */
    public void sendMessage(Message message) {
        if (!active.get()) {
            return;
        }
        
        try {
            writer.println("MESSAGE:" + message.serialize());
        } catch (Exception e) {
            System.err.println("Error sending message to " + getRemoteIdentifier() + ": " + e.getMessage());
            close();
        }
    }
    
    /**
     * Sends handshake with peer name
     */
    public void sendHandshake(String peerName) {
        if (!active.get()) {
            return;
        }
        
        try {
            writer.println("HANDSHAKE:" + peerName);
        } catch (Exception e) {
            System.err.println("Error sending handshake to " + getRemoteIdentifier() + ": " + e.getMessage());
            close();
        }
    }
    
    /**
     * Closes the connection to this peer
     */
    public void close() {
        if (active.compareAndSet(true, false)) {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (socket != null) socket.close();
                
                System.out.println("Disconnected from peer " + getRemoteIdentifier());
                
                // Notify parent peer about disconnection
                parentPeer.removePeerConnection(this);
                
            } catch (IOException e) {
                System.err.println("Error closing connection to " + getRemoteIdentifier() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks if the connection is still active
     */
    public boolean isActive() {
        return active.get() && !socket.isClosed();
    }
    
    /**
     * Gets the remote peer identifier
     */
    public String getRemoteIdentifier() {
        return remoteAddress + ":" + remotePort;
    }
    
    /**
     * Gets the remote peer name (if available after handshake)
     */
    public String getRemotePeerName() {
        return remotePeerName != null ? remotePeerName : getRemoteIdentifier();
    }
    
    @Override
    public String toString() {
        return "PeerConnection{" +
                "remote=" + getRemoteIdentifier() +
                ", name=" + (remotePeerName != null ? remotePeerName : "unknown") +
                ", active=" + active.get() +
                '}';
    }
}