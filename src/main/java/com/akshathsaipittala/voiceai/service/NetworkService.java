package com.akshathsaipittala.voiceai.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);
    private static final String[] TEST_HOSTS = {"8.8.8.8", "1.1.1.1"}; // Google DNS, Cloudflare DNS
    private static final int TEST_PORT = 53; // DNS port
    private static final int TIMEOUT_MS = 3000;

    private volatile boolean lastKnownConnectionState = false;
    private volatile long lastConnectionCheck = 0;
    private static final long CONNECTION_CHECK_INTERVAL = 30000; // 30 seconds

    public boolean isInternetAvailable() {
        long currentTime = System.currentTimeMillis();

        // Use cached result if recent
        if (currentTime - lastConnectionCheck < CONNECTION_CHECK_INTERVAL) {
            return lastKnownConnectionState;
        }

        try {
            boolean isConnected = checkConnectivity();
            lastKnownConnectionState = isConnected;
            lastConnectionCheck = currentTime;
            return isConnected;
        } catch (Exception e) {
            logger.debug("Network connectivity check failed: {}", e.getMessage());
            return false;
        }
    }

    public CompletableFuture<Boolean> isInternetAvailableAsync() {
        return CompletableFuture.supplyAsync(this::isInternetAvailable);
    }

    private boolean checkConnectivity() {
        for (String host : TEST_HOSTS) {
            if (canConnect(host, TEST_PORT)) {
                logger.debug("Internet connectivity confirmed via {}", host);
                return true;
            }
        }
        logger.debug("No internet connectivity detected");
        return false;
    }

    private boolean canConnect(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Check if a specific service URL is reachable
     */
    public boolean isServiceReachable(String host, int port) {
        return canConnect(host, port);
    }

    /**
     * Get network status description
     */
    public String getNetworkStatus() {
        if (isInternetAvailable()) {
            return "Online - Internet connectivity available";
        } else {
            return "Offline - No internet connectivity detected";
        }
    }
}
