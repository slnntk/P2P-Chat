package com.p2pchat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a message in the P2P chat system.
 * Contains sender information, content, and timestamp.
 */
public class Message {
    private final String sender;
    private final String content;
    private final LocalDateTime timestamp;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getFormattedTime() {
        return timestamp.format(formatter);
    }
    
    /**
     * Serializes the message for network transmission
     */
    public String serialize() {
        return sender + "|" + content + "|" + timestamp.toString();
    }
    
    /**
     * Deserializes a message from network transmission
     */
    public static Message deserialize(String data) {
        String[] parts = data.split("\\|", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid message format");
        }
        
        Message msg = new Message(parts[0], parts[1]);
        // We'll use the current time for simplicity, but could parse the timestamp if needed
        return msg;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTime(), sender, content);
    }
}