package com.p2pchat.gui;

import com.p2pchat.Peer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern message input panel with beautiful design and intuitive functionality.
 * Features auto-resizing text area, send button, and keyboard shortcuts.
 */
public class MessageInputPanel extends JPanel {
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue
    private static final Color BORDER_COLOR = new Color(209, 213, 219); // Border gray
    private static final Color TEXT_COLOR = new Color(30, 41, 59); // Dark gray
    private static final Color PLACEHOLDER_COLOR = new Color(156, 163, 175); // Gray
    
    private final Peer peer;
    private JTextArea messageField;
    private JButton sendButton;
    private JScrollPane inputScrollPane;
    private ChatPanel chatPanel; // Reference to update chat
    
    public MessageInputPanel(Peer peer) {
        this.peer = peer;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Create auto-resizing text area
        messageField = new JTextArea(2, 30);
        messageField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        messageField.setBorder(new EmptyBorder(10, 12, 10, 12));
        messageField.setBackground(Color.WHITE);
        
        // Placeholder text functionality
        setPlaceholderText("Type your message here... (Ctrl+Enter to send)");
        
        // Create styled scroll pane for text area
        inputScrollPane = new JScrollPane(messageField);
        inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        inputScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        inputScrollPane.setPreferredSize(new Dimension(0, 60));
        inputScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Create beautiful send button
        sendButton = createStyledButton("Send", PRIMARY_COLOR, Color.WHITE);
        sendButton.setPreferredSize(new Dimension(80, 60));
        sendButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        
        // Add send icon
        sendButton.setIcon(createSendIcon());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(10, 10, 15, 10));
        setBackground(new Color(248, 250, 252));
        
        add(inputScrollPane, BorderLayout.CENTER);
        add(sendButton, BorderLayout.EAST);
    }
    
    private void setupEventHandlers() {
        // Send button click
        sendButton.addActionListener(e -> sendMessage());
        
        // Keyboard shortcuts
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Ctrl+Enter to send
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    e.consume(); // Prevent newline
                    sendMessage();
                }
                // Auto-resize handling
                else if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    // Allow newline with Enter (up to max height)
                    SwingUtilities.invokeLater(this::adjustTextAreaHeight);
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // Remove placeholder when user starts typing
                if (isPlaceholderActive() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                    clearPlaceholder();
                }
            }
            
            private void adjustTextAreaHeight() {
                // Auto-resize text area based on content
                int lines = messageField.getLineCount();
                int maxLines = 6; // Maximum 6 lines
                int minHeight = 60;
                int lineHeight = messageField.getFontMetrics(messageField.getFont()).getHeight();
                
                int newHeight = Math.min(minHeight + (lines - 2) * lineHeight, 
                                       minHeight + (maxLines - 2) * lineHeight);
                
                Dimension currentSize = inputScrollPane.getPreferredSize();
                if (currentSize.height != newHeight) {
                    inputScrollPane.setPreferredSize(new Dimension(currentSize.width, newHeight));
                    revalidate();
                }
            }
        });
        
        // Focus events for placeholder
        messageField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (isPlaceholderActive()) {
                    clearPlaceholder();
                }
            }
            
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (messageField.getText().trim().isEmpty()) {
                    setPlaceholderText("Type your message here... (Ctrl+Enter to send)");
                }
            }
        });
    }
    
    private void sendMessage() {
        String messageText = messageField.getText().trim();
        
        // Don't send empty messages or placeholder text
        if (messageText.isEmpty() || isPlaceholderActive()) {
            return;
        }
        
        // Check if there are any connections
        if (peer.getActiveConnections().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No peer connections available!\nPlease connect to at least one peer before sending messages.",
                "No Connections",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Send the message through the peer
        peer.broadcastMessage(messageText);
        
        // Clear the input field
        messageField.setText("");
        setPlaceholderText("Type your message here... (Ctrl+Enter to send)");
        
        // Reset scroll pane height
        inputScrollPane.setPreferredSize(new Dimension(0, 60));
        revalidate();
        
        // Add system message to chat if available
        if (chatPanel != null) {
            chatPanel.addSystemMessage("Message sent to " + peer.getActiveConnections().size() + " peer(s)");
        }
        
        // Focus back to input field
        SwingUtilities.invokeLater(() -> messageField.requestFocus());
    }
    
    private void setPlaceholderText(String placeholder) {
        messageField.setText(placeholder);
        messageField.setForeground(PLACEHOLDER_COLOR);
        messageField.setFont(messageField.getFont().deriveFont(Font.ITALIC));
    }
    
    private void clearPlaceholder() {
        messageField.setText("");
        messageField.setForeground(TEXT_COLOR);
        messageField.setFont(messageField.getFont().deriveFont(Font.PLAIN));
    }
    
    private boolean isPlaceholderActive() {
        return messageField.getForeground().equals(PLACEHOLDER_COLOR);
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;
            Color hoverColor = bgColor.darker();
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
            
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor.darker());
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
        });
        
        return button;
    }
    
    private Icon createSendIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                
                // Draw send arrow (paper plane style)
                int[] xPoints = {x + 2, x + 12, x + 2, x + 6};
                int[] yPoints = {y + 2, y + 6, y + 10, y + 6};
                g2d.fillPolygon(xPoints, yPoints, 4);
                
                // Draw line across
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(x + 6, y + 6, x + 9, y + 6);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 14; }
            
            @Override
            public int getIconHeight() { return 12; }
        };
    }
    
    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }
    
    public void focusMessageField() {
        SwingUtilities.invokeLater(() -> {
            messageField.requestFocus();
            if (isPlaceholderActive()) {
                clearPlaceholder();
            }
        });
    }
}