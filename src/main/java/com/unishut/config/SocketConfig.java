package com.unishut.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the UniShut GPS TCP server.
 *
 * This class centralizes the TCP socket configuration used
 * by the GPS networking layer.
 *
 * Configuration is loaded from application.properties:
 *
 * unishut.gps.tcp-host
 * unishut.gps.tcp-port
 * unishut.gps.max-connections
 * unishut.gps.max-message-size
 */
@Configuration
public class SocketConfig {

    /**
     * TCP server bind address.
     *
     * 0.0.0.0 means the server listens on all available
     * network interfaces.
     */
    @Value("${unishut.gps.tcp-host:0.0.0.0}")
    private String host;


    /**
     * TCP server port.
     */
    @Value("${unishut.gps.tcp-port:9090}")
    private int port;


    /**
     * Maximum number of simultaneous GPS clients.
     */
    @Value("${unishut.gps.max-connections:50}")
    private int maxConnections;


    /**
     * Maximum size of one incoming GPS message.
     */
    @Value("${unishut.gps.max-message-size:4096}")
    private int maxMessageSize;


    /**
     * Get TCP server host.
     *
     * @return TCP bind address
     */
    public String getHost() {
        return host;
    }


    /**
     * Get TCP server port.
     *
     * @return TCP port
     */
    public int getPort() {
        return port;
    }


    /**
     * Get maximum simultaneous connections.
     *
     * @return maximum connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }


    /**
     * Get maximum incoming message size.
     *
     * @return maximum message size
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }
}