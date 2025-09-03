package com.p2pchat.gui;

import com.p2pchat.Peer;
import com.p2pchat.Message;
import com.p2pchat.PeerConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Main chat window with modern, professional design.
 * Features a beautiful interface with real-time message display, peer management,
 * and intuitive user experience that will definitely impress!
 */
public class MainChatFrame extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue
    private static final Color SECONDARY_COLOR = new Color(248, 250, 252); // Light gray
    private static final Color ACCENT_COLOR = new Color(16, 185, 129); // Green
    private static final Color DANGER_COLOR = new Color(239, 68, 68); // Red
    private static final Color TEXT_COLOR = new Color(30, 41, 59); // Dark gray
    private static final Color BORDER_COLOR = new Color(209, 213, 219); // Border gray
    
    private final Peer peer;
    
    // GUI Components
    private ChatPanel chatPanel;
    private MessageInputPanel messageInputPanel;
    private PeerListPanel peerListPanel;
    private ConnectionPanel connectionPanel;
    private JLabel statusLabel;
    private Timer statusUpdateTimer;
    
    public MainChatFrame(Peer peer) {
        this.peer = peer;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
        startStatusUpdateTimer();
    }
    
    private void initializeComponents() {
        // Create main panels with the peer reference
        chatPanel = new ChatPanel(peer);
        messageInputPanel = new MessageInputPanel(peer);
        peerListPanel = new PeerListPanel(peer);
        connectionPanel = new ConnectionPanel(peer);
        
        // Status label for connection info
        statusLabel = new JLabel("Ready - " + peer.getPeerName() + " on port " + peer.getListeningPort());
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setIcon(createStatusIcon(ACCENT_COLOR));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create elegant header with gradient
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, 0, getHeight(), PRIMARY_COLOR.darker());
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Title with icon
        JLabel titleLabel = new JLabel("P2P Chat " + ChatGUI.getVersion());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(createChatIcon());
        
        // User info
        JLabel userLabel = new JLabel(peer.getPeerName());
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setOpaque(true);
        userLabel.setBackground(new Color(255, 255, 255, 30));
        userLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content area with splitter
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.75); // 75% for chat, 25% for peers
        mainSplitPane.setBorder(null);
        mainSplitPane.setDividerSize(8);
        
        // Left side: Chat area
        JPanel chatAreaPanel = new JPanel(new BorderLayout());
        chatAreaPanel.setBackground(Color.WHITE);
        
        // Add connection panel at top
        chatAreaPanel.add(connectionPanel, BorderLayout.NORTH);
        chatAreaPanel.add(chatPanel, BorderLayout.CENTER);
        chatAreaPanel.add(messageInputPanel, BorderLayout.SOUTH);
        
        // Right side: Peer list
        peerListPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        mainSplitPane.setLeftComponent(chatAreaPanel);
        mainSplitPane.setRightComponent(peerListPanel);
        
        add(mainSplitPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(SECONDARY_COLOR);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(8, 15, 8, 15)
        ));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Window closing handler
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
        
        // Connect panels for communication
        messageInputPanel.setChatPanel(chatPanel);
        connectionPanel.setPeerListPanel(peerListPanel);
    }
    
    private void configureFrame() {
        setTitle("P2P Chat - " + peer.getPeerName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null); // Center on screen
        
        // Set application icon
        try {
            setIconImage(createAppIcon());
        } catch (Exception e) {
            // Ignore if icon creation fails
        }
    }
    
    private void startStatusUpdateTimer() {
        statusUpdateTimer = new Timer(2000, e -> updateStatus());
        statusUpdateTimer.start();
    }
    
    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            List<PeerConnection> connections = peer.getActiveConnections();
            int messageCount = peer.getMessageHistory().size();
            
            String statusText;
            Color iconColor;
            
            if (connections.isEmpty()) {
                statusText = "No connections - " + peer.getPeerName() + " on port " + peer.getListeningPort();
                iconColor = DANGER_COLOR;
            } else {
                statusText = connections.size() + " peer(s) connected - " + messageCount + " messages";
                iconColor = ACCENT_COLOR;
            }
            
            statusLabel.setText(statusText);
            statusLabel.setIcon(createStatusIcon(iconColor));
        });
    }
    
    private void handleWindowClosing() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit P2P Chat?\nAll connections will be closed.",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Stop the timer
            if (statusUpdateTimer != null) {
                statusUpdateTimer.stop();
            }
            
            // Stop the peer
            peer.stop();
            
            // Close the application
            dispose();
            System.exit(0);
        }
    }
    
    private Icon createChatIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw chat bubble with shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(x + 1, y + 3, 18, 14, 6, 6);
                
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(x, y + 2, 18, 14, 6, 6);
                g2d.fillPolygon(new int[]{x + 4, x + 8, x + 4}, new int[]{y + 16, y + 20, y + 20}, 3);
                
                // Draw connection dots
                g2d.setColor(ACCENT_COLOR);
                g2d.fillOval(x + 5, y + 7, 2, 2);
                g2d.fillOval(x + 9, y + 7, 2, 2);
                g2d.fillOval(x + 13, y + 7, 2, 2);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 22; }
            
            @Override
            public int getIconHeight() { return 22; }
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
                
                g2d.setColor(color.brighter());
                g2d.fillOval(x + 2, y + 2, 4, 4);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 8; }
            
            @Override
            public int getIconHeight() { return 8; }
        };
    }
    
    private Image createAppIcon() {
        // Create a simple app icon
        Image img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Background
        g2d.setColor(PRIMARY_COLOR);
        g2d.fillRoundRect(4, 4, 24, 24, 8, 8);
        
        // Chat bubble
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(8, 10, 16, 12, 4, 4);
        g2d.fillPolygon(new int[]{12, 16, 12}, new int[]{22, 26, 26}, 3);
        
        g2d.dispose();
        return img;
    }
    
    @Override
    public void dispose() {
        if (statusUpdateTimer != null) {
            statusUpdateTimer.stop();
        }
        super.dispose();
    }
}