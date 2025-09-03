package com.p2pchat.gui;

import com.p2pchat.Peer;
import com.p2pchat.PeerDiscovery;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Beautiful connection panel for managing peer connections.
 * Features manual connection input, automatic peer discovery, and modern styling.
 */
public class ConnectionPanel extends JPanel {
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue
    private static final Color ACCENT_COLOR = new Color(16, 185, 129); // Green
    private static final Color WARNING_COLOR = new Color(245, 158, 11); // Amber
    private static final Color TEXT_COLOR = new Color(30, 41, 59); // Dark gray
    private static final Color BORDER_COLOR = new Color(209, 213, 219); // Border gray
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251); // Light gray
    
    private final Peer peer;
    private JTextField connectionField;
    private JButton connectButton;
    private JButton discoverButton;
    private PeerListPanel peerListPanel; // Reference to update peer list
    
    public ConnectionPanel(Peer peer) {
        this.peer = peer;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Connection input field with placeholder styling
        connectionField = new JTextField(20);
        connectionField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        connectionField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        connectionField.setToolTipText("Enter host:port (e.g., localhost:8081)");
        
        // Connect button
        connectButton = createStyledButton("Connect", PRIMARY_COLOR, Color.WHITE);
        connectButton.setToolTipText("Connect to the specified peer");
        connectButton.setIcon(createConnectIcon());
        
        // Discover button
        discoverButton = createStyledButton("Discover", ACCENT_COLOR, Color.WHITE);
        discoverButton.setToolTipText("Automatically discover peers on the local network");
        discoverButton.setIcon(createDiscoverIcon());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Title and info
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Peer Connections");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setIcon(createNetworkIcon());
        
        JLabel infoLabel = new JLabel("Connect manually or discover peers automatically");
        infoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        infoLabel.setForeground(new Color(107, 114, 128));
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(infoLabel, BorderLayout.SOUTH);
        
        // Connection controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlsPanel.setOpaque(false);
        
        // Manual connection section
        JLabel connectLabel = new JLabel("Manual:");
        connectLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        connectLabel.setForeground(TEXT_COLOR);
        
        controlsPanel.add(connectLabel);
        controlsPanel.add(connectionField);
        controlsPanel.add(connectButton);
        
        // Separator
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 30));
        controlsPanel.add(separator);
        
        // Auto discovery section
        JLabel discoverLabel = new JLabel("Auto:");
        discoverLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        discoverLabel.setForeground(TEXT_COLOR);
        
        controlsPanel.add(discoverLabel);
        controlsPanel.add(discoverButton);
        
        add(titlePanel, BorderLayout.NORTH);
        add(controlsPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Connect button
        connectButton.addActionListener(e -> handleManualConnection());
        
        // Discover button
        discoverButton.addActionListener(e -> handlePeerDiscovery());
        
        // Enter key in connection field
        connectionField.addActionListener(e -> handleManualConnection());
        
        // Auto-complete for connection field (common localhost ports)
        connectionField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String text = connectionField.getText();
                if (text.equals("8") || text.equals("80") || text.equals("808")) {
                    // Could add auto-completion here if needed
                }
            }
        });
    }
    
    private void handleManualConnection() {
        String input = connectionField.getText().trim();
        
        if (input.isEmpty()) {
            showError("Please enter a host:port to connect to");
            return;
        }
        
        // Parse host:port
        try {
            String[] parts = input.split(":", 2);
            if (parts.length != 2) {
                showError("Invalid format. Use host:port (e.g., localhost:8081)");
                return;
            }
            
            String host = parts[0].trim();
            int port = Integer.parseInt(parts[1].trim());
            
            if (host.isEmpty()) {
                showError("Host cannot be empty");
                return;
            }
            
            if (port == peer.getListeningPort()) {
                showError("Cannot connect to your own port");
                return;
            }
            
            // Disable connect button temporarily
            connectButton.setEnabled(false);
            connectButton.setText("Connecting...");
            
            // Attempt connection in background thread
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return peer.connectToPeer(host, port);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            connectionField.setText("");
                            showSuccess("Connected to " + host + ":" + port);
                            if (peerListPanel != null) {
                                peerListPanel.updatePeerList();
                            }
                        } else {
                            showError("Failed to connect to " + host + ":" + port);
                        }
                    } catch (Exception e) {
                        showError("Connection error: " + e.getMessage());
                    } finally {
                        connectButton.setEnabled(true);
                        connectButton.setText("Connect");
                    }
                }
            };
            
            worker.execute();
            
        } catch (NumberFormatException e) {
            showError("Invalid port number");
        }
    }
    
    private void handlePeerDiscovery() {
        // Disable discover button temporarily
        discoverButton.setEnabled(false);
        discoverButton.setText("Discovering...");
        
        // Run discovery in background
        SwingWorker<List<PeerDiscovery.PeerInfo>, Void> worker = 
                new SwingWorker<List<PeerDiscovery.PeerInfo>, Void>() {
            @Override
            protected List<PeerDiscovery.PeerInfo> doInBackground() throws Exception {
                return PeerDiscovery.discoverLocalPeers(peer.getListeningPort());
            }
            
            @Override
            protected void done() {
                try {
                    List<PeerDiscovery.PeerInfo> discoveries = get();
                    
                    if (discoveries.isEmpty()) {
                        showWarning("No peers found on localhost ports 8080-8090");
                    } else {
                        showDiscoveryDialog(discoveries);
                    }
                    
                } catch (Exception e) {
                    showError("Discovery error: " + e.getMessage());
                } finally {
                    discoverButton.setEnabled(true);
                    discoverButton.setText("Discover");
                }
            }
        };
        
        worker.execute();
    }
    
    private void showDiscoveryDialog(List<PeerDiscovery.PeerInfo> peers) {
        PeerDiscoveryDialog dialog = new PeerDiscoveryDialog(
            SwingUtilities.getWindowAncestor(this), peers, peer, peerListPanel);
        dialog.setVisible(true);
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;
            Color hoverColor = bgColor.darker();
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                }
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    private Icon createConnectIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                
                // Draw connection line
                g2d.drawLine(x + 2, y + 6, x + 10, y + 6);
                
                // Draw endpoints
                g2d.fillOval(x, y + 4, 4, 4);
                g2d.fillOval(x + 8, y + 4, 4, 4);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 12; }
            
            @Override
            public int getIconHeight() { return 12; }
        };
    }
    
    private Icon createDiscoverIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                
                // Draw radar circles
                g2d.drawOval(x + 3, y + 3, 6, 6);
                g2d.drawOval(x + 1, y + 1, 10, 10);
                
                // Draw center dot
                g2d.fillOval(x + 5, y + 5, 2, 2);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 12; }
            
            @Override
            public int getIconHeight() { return 12; }
        };
    }
    
    private Icon createNetworkIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(PRIMARY_COLOR);
                
                // Draw network nodes
                g2d.fillOval(x, y, 6, 6);
                g2d.fillOval(x + 10, y, 6, 6);
                g2d.fillOval(x + 5, y + 8, 6, 6);
                
                // Draw connections
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(x + 3, y + 3, x + 13, y + 3);
                g2d.drawLine(x + 3, y + 3, x + 8, y + 11);
                g2d.drawLine(x + 13, y + 3, x + 8, y + 11);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 16; }
            
            @Override
            public int getIconHeight() { return 14; }
        };
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Connection Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Discovery", JOptionPane.WARNING_MESSAGE);
    }
    
    public void setPeerListPanel(PeerListPanel peerListPanel) {
        this.peerListPanel = peerListPanel;
    }
}