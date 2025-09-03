package com.p2pchat.gui;

import com.p2pchat.Message;
import com.p2pchat.PeerConnection;

/**
 * Observer interface for receiving real-time updates from the Peer.
 * Allows GUI components to be notified of important peer events.
 */
public interface PeerObserver {
    /**
     * Called when a new message is received or sent
     */
    void onMessageReceived(Message message);
    
    /**
     * Called when a peer connection is established
     */
    void onPeerConnected(PeerConnection connection);
    
    /**
     * Called when a peer connection is lost
     */
    void onPeerDisconnected(PeerConnection connection);
    
    /**
     * Called when the peer status changes
     */
    void onStatusChanged();
}