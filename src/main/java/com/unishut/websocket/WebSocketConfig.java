package com.unishut.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for UniShut.
 *
 * The backend uses WebSocket/STOMP for real-time
 * communication between the server and frontend clients.
 *
 * Main flow:
 *
 * GPS Device
 *     |
 *     | TCP
 *     v
 * GpsSocketServer
 *     |
 *     v
 * LocationService
 *     |
 *     v
 * WebSocket
 *     |
 *     v
 * React Live Tracking Map
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {


    /**
     * Configure the in-memory message broker.
     *
     * "/topic" is used for server-to-client broadcasts.
     *
     * Example:
     *
     * /topic/buses
     * /topic/buses/{busId}
     * /topic/trips/{tripId}
     */
    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {

        /*
         * Messages sent to /topic are handled by
         * Spring's simple in-memory broker.
         */
        registry.enableSimpleBroker(
                "/topic"
        );


        /*
         * Application destinations use /app.
         *
         * Example:
         *
         * /app/tracking
         */
        registry.setApplicationDestinationPrefixes(
                "/app"
        );
    }


    /**
     * Register the WebSocket/STOMP endpoint.
     *
     * Frontend clients can connect to:
     *
     * ws://localhost:8080/api/ws
     *
     * SockJS fallback is also enabled.
     */
    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {

        registry.addEndpoint(
                "/ws"
        )
        .setAllowedOriginPatterns(
                "*"
        )
        .withSockJS();
    }
}