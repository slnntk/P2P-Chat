package com.p2pchat.gui;

import com.p2pchat.Peer;
import com.p2pchat.PeerDiscovery;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Beautiful dialog for selecting discovered peers to connect to.
 * Features modern design with hover effects and intuitive peer selection.
 */
public class PeerDiscoveryDialog extends JDialog {
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue
    private static final Color ACCENT_COLOR = new Color(16, 185, 129); // Green
    private static final Color TEXT_COLOR = new Color(30, 41, 59); // Dark gray
    private static final Color SECONDARY_TEXT = new Color(107, 114, 128); // Gray
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251); // Light gray
    private static final Color HOVER_COLOR = new Color(243, 244, 246); // Hover gray
    
    private final List<PeerDiscovery.PeerInfo> discoveredPeers;
    private final Peer peer;
    private final PeerListPanel peerListPanel;
    private JPanel peersContainer;
    
    public PeerDiscoveryDialog(Window parent, List<PeerDiscovery.PeerInfo> discoveredPeers, 
                              Peer peer, PeerListPanel peerListPanel) {
        super(parent, "Discovered Peers", ModalityType.APPLICATION_MODAL);
        this.discoveredPeers = discoveredPeers;
        this.peer = peer;
        this.peerListPanel = peerListPanel;
        
        initializeComponents();
        setupLayout();
        configureDialog();
    }
    
    private void initializeComponents() {
        peersContainer = new JPanel();
        peersContainer.setLayout(new BoxLayout(peersContainer, BoxLayout.Y_AXIS));
        peersContainer.setBackground(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header with gradient
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, ACCENT_COLOR, 0, getHeight(), ACCENT_COLOR.darker());
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel titleLabel = new JLabel("Select Peers to Connect");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(createDiscoveryIcon());
        
        JLabel subtitleLabel = new JLabel("Found " + discoveredPeers.size() + " peer" + 
                                        (discoveredPeers.size() == 1 ? "" : "s") + " on the local network");
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Instructions
        JLabel instructionLabel = new JLabel("<html><div style='text-align: center; color: #6B7280;'>"
                + "Click on a peer below to connect. You can connect to multiple peers."
                + "</div></html>");
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setBorder(new EmptyBorder(0, 20, 15, 20));
        
        // Add peer panels
        populatePeersList();
        
        JScrollPane scrollPane = new JScrollPane(peersContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        contentPanel.add(instructionLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton closeButton = createStyledButton("Close", new Color(156, 163, 175), Color.WHITE);
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void populatePeersList() {
        for (PeerDiscovery.PeerInfo peerInfo : discoveredPeers) {
            JPanel peerPanel = createPeerPanel(peerInfo);
            peersContainer.add(peerPanel);
            peersContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    }
    
    private JPanel createPeerPanel(PeerDiscovery.PeerInfo peerInfo) {
        JPanel peerPanel = new JPanel(new BorderLayout());
        peerPanel.setBackground(Color.WHITE);
        peerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(20, 25, 20, 25)
        ));
        peerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        peerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Left side - Peer info with icon
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        
        JLabel addressLabel = new JLabel(peerInfo.toString());
        addressLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        addressLabel.setForeground(TEXT_COLOR);
        addressLabel.setIcon(createPeerIcon());
        
        JLabel detailLabel = new JLabel("Click to connect to this peer");
        detailLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        detailLabel.setForeground(SECONDARY_TEXT);
        
        infoPanel.add(addressLabel, BorderLayout.NORTH);
        infoPanel.add(detailLabel, BorderLayout.SOUTH);
        
        // Right side - Connect arrow
        JLabel arrowLabel = new JLabel("→");
        arrowLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        arrowLabel.setForeground(PRIMARY_COLOR);
        
        peerPanel.add(infoPanel, BorderLayout.CENTER);
        peerPanel.add(arrowLabel, BorderLayout.EAST);
        
        // Click handler
        peerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                peerPanel.setBackground(HOVER_COLOR);
                arrowLabel.setForeground(PRIMARY_COLOR.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                peerPanel.setBackground(Color.WHITE);
                arrowLabel.setForeground(PRIMARY_COLOR);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                connectToPeer(peerInfo);
            }
        });
        
        return peerPanel;
    }
    
    private void connectToPeer(PeerDiscovery.PeerInfo peerInfo) {
        // Show connecting state
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return peer.connectToPeer(peerInfo.getHost(), peerInfo.getPort());
            }
            
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    boolean success = get();
                    if (success) {
                        showConnectionSuccess(peerInfo);
                        if (peerListPanel != null) {
                            peerListPanel.updatePeerList();
                        }
                    } else {
                        showConnectionError(peerInfo, "Connection failed");
                    }
                } catch (Exception e) {
                    showConnectionError(peerInfo, e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void showConnectionSuccess(PeerDiscovery.PeerInfo peerInfo) {
        JOptionPane.showMessageDialog(this,
            "Successfully connected to " + peerInfo.toString() + "!\n" +
            "You can now start chatting.",
            "Connection Successful",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showConnectionError(PeerDiscovery.PeerInfo peerInfo, String error) {
        JOptionPane.showMessageDialog(this,
            "Failed to connect to " + peerInfo.toString() + "\n" +
            "Error: " + error,
            "Connection Failed",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;
            Color hoverColor = bgColor.darker();
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    private Icon createDiscoveryIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2f));
                
                // Draw radar circles
                g2d.drawOval(x + 4, y + 4, 10, 10);
                g2d.drawOval(x + 2, y + 2, 14, 14);
                g2d.drawOval(x, y, 18, 18);
                
                // Draw center dot
                g2d.fillOval(x + 7, y + 7, 4, 4);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 18; }
            
            @Override
            public int getIconHeight() { return 18; }
        };
    }
    
    private Icon createPeerIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(ACCENT_COLOR);
                // Server/computer icon
                g2d.fillRoundRect(x, y + 2, 14, 10, 2, 2);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(x + 2, y + 4, 10, 6, 1, 1);
                
                // Connection dots
                g2d.setColor(ACCENT_COLOR);
                g2d.fillOval(x + 4, y + 6, 2, 2);
                g2d.fillOval(x + 7, y + 6, 2, 2);
                g2d.fillOval(x + 10, y + 6, 2, 2);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 14; }
            
            @Override
            public int getIconHeight() { return 14; }
        };
    }
    
    private void configureDialog() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(450, Math.min(500, 200 + discoveredPeers.size() * 90));
        setLocationRelativeTo(getParent());
    }
}