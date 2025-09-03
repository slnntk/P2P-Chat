package com.p2pchat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages message history for the P2P chat session.
 * Thread-safe implementation for concurrent access.
 */
public class MessageHistory {
    private final List<Message> messages;
    private final int maxMessages;
    
    public MessageHistory() {
        this(1000); // Default maximum of 1000 messages
    }
    
    public MessageHistory(int maxMessages) {
        this.messages = new CopyOnWriteArrayList<>();
        this.maxMessages = maxMessages;
    }
    
    /**
     * Adds a message to the history
     */
    public void addMessage(Message message) {
        messages.add(message);
        
        // Remove oldest messages if we exceed the limit
        if (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }
    
    /**
     * Gets all messages in chronological order
     */
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
    
    /**
     * Gets the last N messages
     */
    public List<Message> getRecentMessages(int count) {
        int size = messages.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(messages.subList(fromIndex, size));
    }
    
    /**
     * Clears all message history
     */
    public void clear() {
        messages.clear();
    }
    
    /**
     * Gets the total number of messages
     */
    public int size() {
        return messages.size();
    }
    
    /**
     * Displays recent messages to console
     */
    public void displayRecentMessages(int count) {
        List<Message> recent = getRecentMessages(count);
        System.out.println("\n--- Recent Messages ---");
        for (Message msg : recent) {
            System.out.println(msg);
        }
        System.out.println("----------------------\n");
    }
}