package com.p2pchat;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main peer class that handles multiple P2P connections and message broadcasting.
 * Each peer acts as both a server (accepting connections) and client (connecting to others).
 */
public class Peer {
    private final String peerName;
    private final int listeningPort;
    private final List<PeerConnection> connections;
    private final MessageHistory messageHistory;
    private final AtomicBoolean running;
    private ServerSocket serverSocket;
    
    public Peer(String peerName, int listeningPort) {
        this.peerName = peerName;
        this.listeningPort = listeningPort;
        this.connections = new CopyOnWriteArrayList<>();
        this.messageHistory = new MessageHistory();
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * Starts the peer server to accept incoming connections
     */
    public void start() throws IOException {
        if (running.get()) {
            return;
        }
        
        serverSocket = new ServerSocket(listeningPort);
        running.set(true);
        
        System.out.println("Peer '" + peerName + "' started on port " + listeningPort);
        
        // Start accepting connections in a separate thread
        Thread serverThread = new Thread(this::acceptConnections);
        serverThread.setName("PeerServer-" + peerName);
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    /**
     * Accepts incoming peer connections
     */
    private void acceptConnections() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Create new peer connection
                PeerConnection connection = new PeerConnection(clientSocket, this);
                connections.add(connection);
                
                // Send handshake
                connection.sendHandshake(peerName);
                
                System.out.println("New peer connected from " + connection.getRemoteIdentifier());
                
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Connects to another peer
     */
    public boolean connectToPeer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            
            // Create new peer connection
            PeerConnection connection = new PeerConnection(socket, this);
            connections.add(connection);
            
            // Send handshake
            connection.sendHandshake(peerName);
            
            System.out.println("Connected to peer at " + host + ":" + port);
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to connect to " + host + ":" + port + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Broadcasts a message to all connected peers
     */
    public void broadcastMessage(String content) {
        Message message = new Message(peerName, content);
        
        // Add to local message history
        messageHistory.addMessage(message);
        
        // Display locally
        System.out.println(message);
        
        // Send to all connected peers
        for (PeerConnection connection : connections) {
            if (connection.isActive()) {
                connection.sendMessage(message);
            }
        }
    }
    
    /**
     * Handles receiving a message from a peer
     */
    public void receiveMessage(Message message, PeerConnection fromConnection) {
        // Add to message history
        messageHistory.addMessage(message);
        
        // Display the message
        System.out.println(message);
        
        // Forward the message to all other peers (excluding the sender)
        for (PeerConnection connection : connections) {
            if (connection != fromConnection && connection.isActive()) {
                connection.sendMessage(message);
            }
        }
    }
    
    /**
     * Removes a peer connection (called when connection is closed)
     */
    public void removePeerConnection(PeerConnection connection) {
        connections.remove(connection);
    }
    
    /**
     * Gets the list of active connections
     */
    public List<PeerConnection> getActiveConnections() {
        return connections.stream()
                .filter(PeerConnection::isActive)
                .toList();
    }
    
    /**
     * Gets the message history
     */
    public MessageHistory getMessageHistory() {
        return messageHistory;
    }
    
    /**
     * Displays current peer status
     */
    public void displayStatus() {
        System.out.println("\n=== Peer Status ===");
        System.out.println("Name: " + peerName);
        System.out.println("Listening Port: " + listeningPort);
        System.out.println("Active Connections: " + getActiveConnections().size());
        
        for (PeerConnection conn : getActiveConnections()) {
            System.out.println("  - " + conn.getRemotePeerName());
        }
        
        System.out.println("Messages in History: " + messageHistory.size());
        System.out.println("==================\n");
    }
    
    /**
     * Stops the peer and closes all connections
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        
        System.out.println("Shutting down peer '" + peerName + "'...");
        
        // Close all peer connections
        for (PeerConnection connection : connections) {
            connection.close();
        }
        connections.clear();
        
        // Close server socket
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        System.out.println("Peer '" + peerName + "' stopped.");
    }
    
    /**
     * Checks if the peer is running
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Gets the peer name
     */
    public String getPeerName() {
        return peerName;
    }
    
    /**
     * Gets the listening port
     */
    public int getListeningPort() {
        return listeningPort;
    }
}