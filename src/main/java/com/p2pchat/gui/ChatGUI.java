package com.p2pchat.gui;

import com.p2pchat.Peer;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Main GUI controller for the P2P Chat system.
 * Provides a modern Swing interface with beautiful design and excellent user experience.
 * 
 * This class serves as the main entry point for the GUI version of the chat application,
 * maintaining clean separation between the business logic (Peer classes) and presentation layer.
 */
public class ChatGUI {
    private static final String VERSION = "2.0.0 GUI";
    
    private MainChatFrame mainFrame;
    private Peer peer;
    
    /**
     * Main entry point for the GUI application
     */
    public static void main(String[] args) {
        // Set look and feel to system native
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to default look and feel
        }
        
        // Ensure GUI operations happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new ChatGUI().startApplication();
        });
    }
    
    /**
     * Initializes and displays the main application window
     */
    public void startApplication() {
        try {
            // Show welcome dialog to get user information
            UserSetupDialog setupDialog = new UserSetupDialog();
            setupDialog.setVisible(true);
            
            if (setupDialog.isOk()) {
                String userName = setupDialog.getUserName();
                int port = setupDialog.getPort();
                
                // Create and start the peer
                peer = new Peer(userName, port);
                peer.start();
                
                // Create and show main chat window
                mainFrame = new MainChatFrame(peer);
                mainFrame.setVisible(true);
                
                // Set up shutdown hook for graceful exit
                Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
                
            } else {
                // User cancelled, exit application
                System.exit(0);
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null, 
                "Failed to start P2P Chat:\n" + e.getMessage() + 
                "\n\nPlease try a different port.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }
    
    /**
     * Gracefully shuts down the application
     */
    private void shutdown() {
        if (peer != null) {
            peer.stop();
        }
        
        if (mainFrame != null) {
            mainFrame.dispose();
        }
    }
    
    /**
     * Gets the application version
     */
    public static String getVersion() {
        return VERSION;
    }
}