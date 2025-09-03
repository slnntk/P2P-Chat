package com.p2pchat;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Main chat client application with console interface.
 * Provides user interaction for the P2P chat system.
 */
public class ChatClient {
    private static final String VERSION = "1.0.0";
    private Peer peer;
    private Scanner scanner;
    private boolean running;
    
    public ChatClient() {
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.run();
    }
    
    /**
     * Main application loop
     */
    public void run() {
        printWelcome();
        
        // Get user information
        String userName = getUserName();
        int port = getUserPort();
        
        try {
            // Create and start the peer
            peer = new Peer(userName, port);
            peer.start();
            
            // Set up shutdown hook for graceful exit
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            
            // Show main menu
            showMainMenu();
            
        } catch (IOException e) {
            System.err.println("Failed to start peer: " + e.getMessage());
            System.err.println("Please try a different port.");
        }
        
        shutdown();
    }
    
    /**
     * Displays welcome message and instructions
     */
    private void printWelcome() {
        System.out.println("========================================");
        System.out.println("    P2P Chat System v" + VERSION);
        System.out.println("========================================");
        System.out.println("Welcome to the P2P Chat System!");
        System.out.println("This application allows you to chat with");
        System.out.println("multiple users in a decentralized network.");
        System.out.println("========================================\n");
    }
    
    /**
     * Gets the username from user input
     */
    private String getUserName() {
        System.out.print("Enter your username: ");
        return scanner.nextLine().trim();
    }
    
    /**
     * Gets the listening port from user input
     */
    private int getUserPort() {
        while (true) {
            System.out.print("Enter listening port (8080-8090): ");
            try {
                int port = Integer.parseInt(scanner.nextLine().trim());
                if (port >= 8080 && port <= 8090) {
                    return port;
                } else {
                    System.out.println("Please enter a port between 8080 and 8090.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid port number.");
            }
        }
    }
    
    /**
     * Shows the main menu and handles user commands
     */
    private void showMainMenu() {
        System.out.println("\nPeer started successfully!");
        printHelp();
        
        while (running && peer.isRunning()) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            processCommand(input);
        }
    }
    
    /**
     * Processes user commands
     */
    private void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "help":
            case "h":
                printHelp();
                break;
                
            case "connect":
            case "c":
                handleConnect(parts);
                break;
                
            case "discover":
            case "d":
                handleDiscover();
                break;
                
            case "status":
            case "s":
                peer.displayStatus();
                break;
                
            case "history":
            case "hist":
                handleHistory(parts);
                break;
                
            case "clear":
                clearScreen();
                break;
                
            case "quit":
            case "exit":
            case "q":
                running = false;
                break;
                
            default:
                // Treat as a chat message
                if (!input.startsWith("/")) {
                    peer.broadcastMessage(input);
                } else {
                    System.out.println("Unknown command. Type 'help' for available commands.");
                }
                break;
        }
    }
    
    /**
     * Handles the connect command
     */
    private void handleConnect(String[] parts) {
        if (parts.length < 2) {
            System.out.print("Enter host:port to connect to: ");
            String hostPort = scanner.nextLine().trim();
            if (!hostPort.isEmpty()) {
                connectToPeer(hostPort);
            }
        } else {
            connectToPeer(parts[1]);
        }
    }
    
    /**
     * Connects to a peer given host:port string
     */
    private void connectToPeer(String hostPort) {
        try {
            String[] parts = hostPort.split(":", 2);
            if (parts.length != 2) {
                System.out.println("Invalid format. Use: host:port (e.g., localhost:8081)");
                return;
            }
            
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            peer.connectToPeer(host, port);
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
        }
    }
    
    /**
     * Handles the discover command to find peers automatically
     */
    private void handleDiscover() {
        System.out.println("Discovering peers...");
        
        List<PeerDiscovery.PeerInfo> localPeers = PeerDiscovery.discoverLocalPeers(peer.getListeningPort());
        
        if (localPeers.isEmpty()) {
            System.out.println("No peers found on localhost.");
        } else {
            System.out.println("Found " + localPeers.size() + " peer(s):");
            for (int i = 0; i < localPeers.size(); i++) {
                PeerDiscovery.PeerInfo peerInfo = localPeers.get(i);
                System.out.println((i + 1) + ". " + peerInfo);
            }
            
            System.out.print("Enter number to connect (or 0 to cancel): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice > 0 && choice <= localPeers.size()) {
                    PeerDiscovery.PeerInfo selected = localPeers.get(choice - 1);
                    peer.connectToPeer(selected.getHost(), selected.getPort());
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection.");
            }
        }
    }
    
    /**
     * Handles the history command
     */
    private void handleHistory(String[] parts) {
        int count = 10; // Default to last 10 messages
        
        if (parts.length > 1) {
            try {
                count = Integer.parseInt(parts[1]);
                count = Math.max(1, Math.min(count, 100)); // Limit between 1 and 100
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Using default of 10 messages.");
            }
        }
        
        peer.getMessageHistory().displayRecentMessages(count);
    }
    
    /**
     * Clears the console screen
     */
    private void clearScreen() {
        // Try to clear screen (works on most terminals)
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }
    
    /**
     * Prints help information
     */
    private void printHelp() {
        System.out.println("\n=== P2P Chat Commands ===");
        System.out.println("connect <host:port>  - Connect to a peer (e.g., connect localhost:8081)");
        System.out.println("discover             - Find and connect to peers automatically");
        System.out.println("status               - Show peer status and connections");
        System.out.println("history [count]      - Show message history (default: 10 messages)");
        System.out.println("clear                - Clear the screen");
        System.out.println("help                 - Show this help message");
        System.out.println("quit                 - Exit the application");
        System.out.println();
        System.out.println("To send a message, just type it and press Enter.");
        System.out.println("Messages will be broadcasted to all connected peers.");
        System.out.println("========================\n");
    }
    
    /**
     * Gracefully shuts down the application
     */
    private void shutdown() {
        running = false;
        
        if (peer != null) {
            peer.stop();
        }
        
        if (scanner != null) {
            scanner.close();
        }
        
        System.out.println("Thank you for using P2P Chat!");
    }
}