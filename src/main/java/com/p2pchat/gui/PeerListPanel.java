package com.p2pchat.gui;

import com.p2pchat.Peer;
import com.p2pchat.PeerConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Beautiful peer list panel showing connected peers with status indicators.
 * Features real-time updates, modern design, and interactive peer information.
 */
public class PeerListPanel extends JPanel {
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue
    private static final Color ACCENT_COLOR = new Color(16, 185, 129); // Green
    private static final Color TEXT_COLOR = new Color(30, 41, 59); // Dark gray
    private static final Color SECONDARY_TEXT = new Color(107, 114, 128); // Gray
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251); // Light gray
    private static final Color HOVER_COLOR = new Color(243, 244, 246); // Hover gray
    
    private final Peer peer;
    private JPanel peerListContainer;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private Timer updateTimer;
    
    public PeerListPanel(Peer peer) {
        this.peer = peer;
        initializeComponents();
        setupLayout();
        startAutoUpdate();
    }
    
    private void initializeComponents() {
        // Header label
        headerLabel = new JLabel("Connected Peers");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setIcon(createPeerIcon());
        
        // Status label
        statusLabel = new JLabel("No connections");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(SECONDARY_TEXT);
        
        // Container for peer list
        peerListContainer = new JPanel();
        peerListContainer.setLayout(new BoxLayout(peerListContainer, BoxLayout.Y_AXIS));
        peerListContainer.setBackground(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Scroll pane for peer list
        JScrollPane scrollPane = new JScrollPane(peerListContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Empty state panel
        JPanel emptyStatePanel = new JPanel(new BorderLayout());
        emptyStatePanel.setBackground(Color.WHITE);
        emptyStatePanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        JLabel emptyIcon = new JLabel("🌐", JLabel.CENTER);
        emptyIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
        
        JLabel emptyText = new JLabel("<html><div style='text-align: center;'>"
                + "No peer connections yet<br>"
                + "<small>Use the connection panel above to connect with other users</small>"
                + "</div></html>", JLabel.CENTER);
        emptyText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        emptyText.setForeground(SECONDARY_TEXT);
        
        emptyStatePanel.add(emptyIcon, BorderLayout.NORTH);
        emptyStatePanel.add(emptyText, BorderLayout.CENTER);
        
        peerListContainer.add(emptyStatePanel);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void startAutoUpdate() {
        updateTimer = new Timer(2000, e -> updatePeerList());
        updateTimer.start();
    }
    
    public void updatePeerList() {
        SwingUtilities.invokeLater(() -> {
            List<PeerConnection> connections = peer.getActiveConnections();
            
            // Update status label
            if (connections.isEmpty()) {
                statusLabel.setText("No connections");
                statusLabel.setIcon(createStatusIcon(new Color(239, 68, 68))); // Red
            } else {
                statusLabel.setText(connections.size() + " active connection" + 
                                  (connections.size() == 1 ? "" : "s"));
                statusLabel.setIcon(createStatusIcon(ACCENT_COLOR)); // Green
            }
            
            // Clear container
            peerListContainer.removeAll();
            
            if (connections.isEmpty()) {
                // Show empty state
                JPanel emptyStatePanel = createEmptyStatePanel();
                peerListContainer.add(emptyStatePanel);
            } else {
                // Add peer items
                for (PeerConnection connection : connections) {
                    JPanel peerPanel = createPeerPanel(connection);
                    peerListContainer.add(peerPanel);
                    peerListContainer.add(Box.createRigidArea(new Dimension(0, 5)));
                }
                
                // Add some spacing at the bottom
                peerListContainer.add(Box.createVerticalGlue());
            }
            
            revalidate();
            repaint();
        });
    }
    
    private JPanel createEmptyStatePanel() {
        JPanel emptyStatePanel = new JPanel(new BorderLayout());
        emptyStatePanel.setBackground(Color.WHITE);
        emptyStatePanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        JLabel emptyIcon = new JLabel("🌐", JLabel.CENTER);
        emptyIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
        
        JLabel emptyText = new JLabel("<html><div style='text-align: center;'>"
                + "No peer connections yet<br>"
                + "<small style='color: #6B7280;'>Use the connection panel above to connect with other users</small>"
                + "</div></html>", JLabel.CENTER);
        emptyText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        emptyStatePanel.add(emptyIcon, BorderLayout.NORTH);
        emptyStatePanel.add(emptyText, BorderLayout.CENTER);
        
        return emptyStatePanel;
    }
    
    private JPanel createPeerPanel(PeerConnection connection) {
        JPanel peerPanel = new JPanel(new BorderLayout());
        peerPanel.setBackground(Color.WHITE);
        peerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        peerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Left side - User info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        
        // Peer name with icon
        JLabel nameLabel = new JLabel(connection.getRemotePeerName() != null ? 
                                     connection.getRemotePeerName() : "Unknown User");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setIcon(createUserIcon());
        
        // Connection info
        JLabel addressLabel = new JLabel(connection.getRemoteIdentifier());
        addressLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        addressLabel.setForeground(SECONDARY_TEXT);
        
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(addressLabel, BorderLayout.SOUTH);
        
        // Right side - Status indicator
        JLabel statusIndicator = new JLabel();
        statusIndicator.setIcon(createStatusIcon(ACCENT_COLOR));
        statusIndicator.setToolTipText("Connected");
        
        peerPanel.add(infoPanel, BorderLayout.CENTER);
        peerPanel.add(statusIndicator, BorderLayout.EAST);
        
        // Hover effects
        peerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                peerPanel.setBackground(HOVER_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                peerPanel.setBackground(Color.WHITE);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                showPeerDetails(connection);
            }
        });
        
        return peerPanel;
    }
    
    private void showPeerDetails(PeerConnection connection) {
        String details = String.format(
            "Peer Information:\n\n" +
            "Name: %s\n" +
            "Address: %s\n" +
            "Status: %s\n" +
            "Connection: Active",
            connection.getRemotePeerName() != null ? connection.getRemotePeerName() : "Unknown",
            connection.getRemoteIdentifier(),
            "Connected"
        );
        
        JOptionPane.showMessageDialog(this, details, "Peer Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private Icon createPeerIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw multiple user icons
                g2d.setColor(PRIMARY_COLOR);
                
                // First user
                g2d.fillOval(x, y + 2, 8, 8);
                g2d.fillRoundRect(x - 2, y + 8, 12, 6, 3, 3);
                
                // Second user (offset)
                g2d.setColor(ACCENT_COLOR);
                g2d.fillOval(x + 6, y + 2, 8, 8);
                g2d.fillRoundRect(x + 4, y + 8, 12, 6, 3, 3);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 18; }
            
            @Override
            public int getIconHeight() { return 16; }
        };
    }
    
    private Icon createUserIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(PRIMARY_COLOR);
                // Head
                g2d.fillOval(x + 3, y + 1, 6, 6);
                // Body
                g2d.fillRoundRect(x, y + 6, 12, 8, 3, 3);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 12; }
            
            @Override
            public int getIconHeight() { return 14; }
        };
    }
    
    private Icon createStatusIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(color);
                g2d.fillOval(x, y, 8, 8);
                
                g2d.setColor(Color.WHITE);
                g2d.fillOval(x + 2, y + 2, 4, 4);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 8; }
            
            @Override
            public int getIconHeight() { return 8; }
        };
    }
    
    @Override
    public void removeNotify() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        super.removeNotify();
    }
}