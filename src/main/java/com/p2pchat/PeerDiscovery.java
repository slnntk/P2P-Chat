package com.p2pchat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Simple peer discovery mechanism for finding other peers on the local network.
 * Scans common ports and localhost for active peers.
 */
public class PeerDiscovery {
    private static final int[] COMMON_PORTS = {8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 8089, 8090};
    private static final int DISCOVERY_TIMEOUT_MS = 1000; // 1 second timeout per connection
    
    /**
     * Discovers active peers on localhost using common ports
     */
    public static List<PeerInfo> discoverLocalPeers(int excludePort) {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<PeerInfo>> futures = new ArrayList<>();
        
        // Test each common port
        for (int port : COMMON_PORTS) {
            if (port != excludePort) { // Don't try to connect to ourselves
                CompletableFuture<PeerInfo> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        Socket testSocket = new Socket();
                        testSocket.connect(new java.net.InetSocketAddress("localhost", port), DISCOVERY_TIMEOUT_MS);
                        testSocket.close();
                        return new PeerInfo("localhost", port);
                    } catch (IOException e) {
                        return null; // No peer at this port
                    }
                }, executor);
                futures.add(future);
            }
        }
        
        // Collect results
        for (CompletableFuture<PeerInfo> future : futures) {
            try {
                PeerInfo peerInfo = future.get(DISCOVERY_TIMEOUT_MS + 500, TimeUnit.MILLISECONDS);
                if (peerInfo != null) {
                    discoveredPeers.add(peerInfo);
                }
            } catch (Exception e) {
                // Ignore failed discoveries
            }
        }
        
        executor.shutdown();
        return discoveredPeers;
    }
    
    /**
     * Discovers peers on the local network (same subnet)
     * This is a more advanced feature that scans the local network
     */
    public static List<PeerInfo> discoverNetworkPeers(int excludePort) {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            String subnet = getSubnet(localAddress.getHostAddress());
            
            ExecutorService executor = Executors.newFixedThreadPool(50);
            List<CompletableFuture<List<PeerInfo>>> futures = new ArrayList<>();
            
            // Scan the subnet (e.g., 192.168.1.1 to 192.168.1.254)
            for (int i = 1; i <= 254; i++) {
                final String targetIP = subnet + i;
                CompletableFuture<List<PeerInfo>> future = CompletableFuture.supplyAsync(() -> {
                    List<PeerInfo> peersAtIP = new ArrayList<>();
                    for (int port : COMMON_PORTS) {
                        if (port != excludePort || !targetIP.equals("localhost")) {
                            try {
                                Socket testSocket = new Socket();
                                testSocket.connect(new java.net.InetSocketAddress(targetIP, port), 500);
                                testSocket.close();
                                peersAtIP.add(new PeerInfo(targetIP, port));
                            } catch (IOException e) {
                                // No peer at this IP:port combination
                            }
                        }
                    }
                    return peersAtIP;
                }, executor);
                futures.add(future);
            }
            
            // Collect results with timeout
            for (CompletableFuture<List<PeerInfo>> future : futures) {
                try {
                    List<PeerInfo> peersAtIP = future.get(2000, TimeUnit.MILLISECONDS);
                    discoveredPeers.addAll(peersAtIP);
                } catch (Exception e) {
                    // Ignore failed discoveries
                }
            }
            
            executor.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error during network discovery: " + e.getMessage());
        }
        
        return discoveredPeers;
    }
    
    /**
     * Extracts subnet from IP address (e.g., "192.168.1.100" -> "192.168.1.")
     */
    private static String getSubnet(String ipAddress) {
        int lastDot = ipAddress.lastIndexOf('.');
        if (lastDot > 0) {
            return ipAddress.substring(0, lastDot + 1);
        }
        return "192.168.1."; // Default fallback
    }
    
    /**
     * Represents information about a discovered peer
     */
    public static class PeerInfo {
        private final String host;
        private final int port;
        
        public PeerInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }
        
        public String getHost() {
            return host;
        }
        
        public int getPort() {
            return port;
        }
        
        @Override
        public String toString() {
            return host + ":" + port;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            PeerInfo peerInfo = (PeerInfo) obj;
            return port == peerInfo.port && host.equals(peerInfo.host);
        }
        
        @Override
        public int hashCode() {
            return host.hashCode() * 31 + port;
        }
    }
}