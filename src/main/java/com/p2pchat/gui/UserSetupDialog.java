package com.p2pchat.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Beautiful setup dialog for gathering user information before starting the chat.
 * Features modern design with professional styling and user-friendly interface.
 */
public class UserSetupDialog extends JDialog {
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue
    private static final Color SECONDARY_COLOR = new Color(248, 250, 252); // Light gray
    private static final Color TEXT_COLOR = new Color(30, 41, 59); // Dark gray
    private static final Color ACCENT_COLOR = new Color(16, 185, 129); // Green
    
    private JTextField userNameField;
    private JSpinner portSpinner;
    private boolean isOk = false;
    
    public UserSetupDialog() {
        super((Frame) null, "P2P Chat Setup", true);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Create modern-styled components
        userNameField = new JTextField(15);
        userNameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Port spinner with validation
        portSpinner = new JSpinner(new SpinnerNumberModel(8080, 8080, 8090, 1));
        portSpinner.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) portSpinner.getEditor();
        editor.getTextField().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel with title and icon
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        headerPanel.setBackground(PRIMARY_COLOR);
        
        JLabel titleLabel = new JLabel("Welcome to P2P Chat " + ChatGUI.getVersion());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(createChatIcon());
        
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(SECONDARY_COLOR);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("<html><div style='text-align: center;'>"
                + "Set up your chat profile to start connecting with peers<br>"
                + "in the decentralized P2P network."
                + "</div></html>");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 25, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(welcomeLabel, gbc);
        
        // Username field
        JLabel userNameLabel = new JLabel("Username:");
        userNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        userNameLabel.setForeground(TEXT_COLOR);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(userNameLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(userNameField, gbc);
        
        // Port field
        JLabel portLabel = new JLabel("Listening Port:");
        portLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        portLabel.setForeground(TEXT_COLOR);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        contentPanel.add(portLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 10, 0);
        contentPanel.add(portSpinner, gbc);
        
        // Port info
        JLabel portInfo = new JLabel("<html><div style='color: #6B7280; font-size: 11px;'>"
                + "Choose a port between 8080-8090 for connecting with other users"
                + "</div></html>");
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(portInfo, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(SECONDARY_COLOR);
        
        JButton startButton = createStyledButton("Start Chat", ACCENT_COLOR, Color.WHITE, true);
        JButton cancelButton = createStyledButton("Cancel", new Color(156, 163, 175), Color.WHITE, false);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(startButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, isPrimary ? Font.BOLD : Font.PLAIN, 13));
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private Icon createChatIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw chat bubble
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(x, y + 2, 14, 10, 4, 4);
                g2d.fillPolygon(new int[]{x + 3, x + 6, x + 3}, new int[]{y + 12, y + 15, y + 15}, 3);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() { return 16; }
            
            @Override
            public int getIconHeight() { return 16; }
        };
    }
    
    private void setupEventHandlers() {
        // Start button action
        ((JButton) ((JPanel) getContentPane().getComponent(2)).getComponent(1)).addActionListener(e -> {
            if (validateInput()) {
                isOk = true;
                dispose();
            }
        });
        
        // Cancel button action
        ((JButton) ((JPanel) getContentPane().getComponent(2)).getComponent(0)).addActionListener(e -> {
            isOk = false;
            dispose();
        });
        
        // Enter key handling
        userNameField.addActionListener(e -> {
            if (validateInput()) {
                isOk = true;
                dispose();
            }
        });
    }
    
    private boolean validateInput() {
        String userName = userNameField.getText().trim();
        if (userName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a username.", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            userNameField.requestFocus();
            return false;
        }
        
        if (userName.length() > 20) {
            JOptionPane.showMessageDialog(this, 
                "Username must be 20 characters or less.", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            userNameField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void configureDialog() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Center on screen
        
        // Set focus to username field
        SwingUtilities.invokeLater(() -> userNameField.requestFocus());
    }
    
    public boolean isOk() {
        return isOk;
    }
    
    public String getUserName() {
        return userNameField.getText().trim();
    }
    
    public int getPort() {
        return (Integer) portSpinner.getValue();
    }
}