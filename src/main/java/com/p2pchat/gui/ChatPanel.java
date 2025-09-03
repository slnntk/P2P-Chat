package com.p2pchat.gui;

import com.p2pchat.Peer;
import com.p2pchat.Message;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

/**
 * Beautiful chat panel for displaying messages with modern styling.
 * Features real-time message updates, custom message rendering, and smooth scrolling.
 */
public class ChatPanel extends JPanel {
    private static final Color BUBBLE_OWN = new Color(37, 99, 235); // Blue for own messages
    private static final Color BUBBLE_OTHER = new Color(249, 250, 251); // Light gray for other messages
    private static final Color TEXT_OWN = Color.WHITE;
    private static final Color TEXT_OTHER = new Color(30, 41, 59);
    private static final Color TIME_COLOR = new Color(107, 114, 128);
    private static final Color SYSTEM_COLOR = new Color(16, 185, 129); // Green for system messages
    
    private final Peer peer;
    private JTextPane messageArea;
    private StyledDocument document;
    private JScrollPane scrollPane;
    private Timer refreshTimer;
    private int lastMessageCount = 0;
    
    public ChatPanel(Peer peer) {
        this.peer = peer;
        initializeComponents();
        setupLayout();
        startAutoRefresh();
    }
    
    private void initializeComponents() {
        messageArea = new JTextPane();
        messageArea.setEditable(false);
        messageArea.setBackground(Color.WHITE);
        messageArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        messageArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        document = messageArea.getStyledDocument();
        
        // Create styled scroll pane
        scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Custom scrollbar styling
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        addWelcomeMessage();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        // Auto-scroll to bottom when messages are added
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scrollToBottom();
            }
        });
    }
    
    private void addWelcomeMessage() {
        try {
            // Add welcome message with beautiful styling
            Style systemStyle = document.addStyle("system", null);
            StyleConstants.setForeground(systemStyle, SYSTEM_COLOR);
            StyleConstants.setBold(systemStyle, true);
            StyleConstants.setFontSize(systemStyle, 13);
            StyleConstants.setAlignment(systemStyle, StyleConstants.ALIGN_CENTER);
            
            Style welcomeStyle = document.addStyle("welcome", null);
            StyleConstants.setForeground(welcomeStyle, new Color(107, 114, 128));
            StyleConstants.setFontSize(welcomeStyle, 12);
            StyleConstants.setAlignment(welcomeStyle, StyleConstants.ALIGN_CENTER);
            StyleConstants.setItalic(welcomeStyle, true);
            
            document.insertString(document.getLength(), "🎉 Welcome to P2P Chat!\n", systemStyle);
            document.insertString(document.getLength(), "Start connecting with peers to begin chatting.\n\n", welcomeStyle);
            
        } catch (BadLocationException e) {
            // Ignore styling errors
        }
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(1000, e -> refreshMessages());
        refreshTimer.start();
    }
    
    public void refreshMessages() {
        List<Message> messages = peer.getMessageHistory().getAllMessages();
        
        // Only update if there are new messages
        if (messages.size() != lastMessageCount) {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Clear existing content except welcome message
                    int welcomeLength = getWelcomeMessageLength();
                    document.remove(welcomeLength, document.getLength() - welcomeLength);
                    
                    // Add all messages with beautiful styling
                    for (Message message : messages) {
                        addStyledMessage(message);
                    }
                    
                    scrollToBottom();
                    lastMessageCount = messages.size();
                    
                } catch (BadLocationException e) {
                    // Handle error gracefully
                }
            });
        }
    }
    
    private int getWelcomeMessageLength() {
        try {
            String text = document.getText(0, document.getLength());
            int endOfWelcome = text.indexOf("\n\n");
            return endOfWelcome > 0 ? endOfWelcome + 2 : 0;
        } catch (BadLocationException e) {
            return 0;
        }
    }
    
    private void addStyledMessage(Message message) throws BadLocationException {
        boolean isOwnMessage = message.getSender().equals(peer.getPeerName());
        
        // Create paragraph style
        Style paragraphStyle = document.addStyle("paragraph", null);
        StyleConstants.setAlignment(paragraphStyle, isOwnMessage ? 
            StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
        
        // Time style
        Style timeStyle = document.addStyle("time", null);
        StyleConstants.setForeground(timeStyle, TIME_COLOR);
        StyleConstants.setFontSize(timeStyle, 11);
        StyleConstants.setItalic(timeStyle, true);
        
        // Sender style
        Style senderStyle = document.addStyle("sender", null);
        StyleConstants.setForeground(senderStyle, isOwnMessage ? BUBBLE_OWN : new Color(59, 130, 246));
        StyleConstants.setBold(senderStyle, true);
        StyleConstants.setFontSize(senderStyle, 13);
        
        // Message content style
        Style contentStyle = document.addStyle("content", null);
        StyleConstants.setForeground(contentStyle, isOwnMessage ? TEXT_OWN : TEXT_OTHER);
        StyleConstants.setFontSize(contentStyle, 14);
        
        // Message bubble background (simulated with spacing)
        String indent = isOwnMessage ? "                    " : "";
        String outdent = isOwnMessage ? "" : "                    ";
        
        // Insert time
        document.insertString(document.getLength(), 
            indent + "[" + message.getFormattedTime() + "] " + outdent + "\n", timeStyle);
        
        // Insert sender and message with bubble effect
        if (isOwnMessage) {
            document.insertString(document.getLength(), 
                indent + "You: " + message.getContent() + "\n\n", contentStyle);
        } else {
            document.insertString(document.getLength(), 
                message.getSender() + ": " + message.getContent() + outdent + "\n\n", contentStyle);
        }
        
        // Apply paragraph alignment
        document.setParagraphAttributes(document.getLength() - 1, 1, paragraphStyle, false);
    }
    
    public void addSystemMessage(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style systemStyle = document.addStyle("system_msg", null);
                StyleConstants.setForeground(systemStyle, SYSTEM_COLOR);
                StyleConstants.setBold(systemStyle, true);
                StyleConstants.setFontSize(systemStyle, 12);
                StyleConstants.setAlignment(systemStyle, StyleConstants.ALIGN_CENTER);
                
                document.insertString(document.getLength(), "• " + text + "\n\n", systemStyle);
                scrollToBottom();
                
            } catch (BadLocationException e) {
                // Handle error gracefully
            }
        });
    }
    
    public void clearMessages() {
        SwingUtilities.invokeLater(() -> {
            try {
                document.remove(0, document.getLength());
                addWelcomeMessage();
                lastMessageCount = 0;
            } catch (BadLocationException e) {
                // Handle error gracefully
            }
        });
    }
    
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }
    
    /**
     * Custom scroll bar UI for modern appearance
     */
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(156, 163, 175);
            this.trackColor = new Color(243, 244, 246);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }
        
        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(thumbColor);
            g2d.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, 
                            thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
            g2d.dispose();
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(trackColor);
            g2d.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2d.dispose();
        }
    }
    
    @Override
    public void removeNotify() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.removeNotify();
    }
}