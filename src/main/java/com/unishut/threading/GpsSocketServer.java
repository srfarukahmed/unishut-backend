package com.unishut.threading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unishut.config.SocketConfig;
import com.unishut.dto.BusLocationDTO;
import com.unishut.service.LocationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP server responsible for receiving GPS location
 * information from UniShut bus tracking devices.
 *
 * Networking:
 *
 *     GPS Client
 *          |
 *          | TCP
 *          v
 *     ServerSocket
 *
 * Multithreading:
 *
 *     Client 1 -> Worker Thread 1
 *     Client 2 -> Worker Thread 2
 *     Client 3 -> Worker Thread 3
 *
 * Each connected GPS client is processed independently.
 *
 * Expected incoming message format:
 *
 * {
 *   "busId": 1,
 *   "tripId": 5,
 *   "latitude": 23.8103,
 *   "longitude": 90.4125,
 *   "timestamp": "2026-09-22T10:30:00",
 *   "source": "GPS_DEVICE"
 * }
 */
@Component
public class GpsSocketServer {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    GpsSocketServer.class
            );


    /**
     * TCP socket configuration.
     */
    private final SocketConfig socketConfig;


    /**
     * Location processing service.
     */
    private final LocationService locationService;


    /**
     * Jackson JSON serializer/deserializer.
     */
    private final ObjectMapper objectMapper;


    /**
     * Actual TCP server socket.
     */
    private volatile ServerSocket serverSocket;


    /**
     * Controls the server accept loop.
     */
    private final AtomicBoolean running =
            new AtomicBoolean(false);


    /**
     * Worker thread pool used for GPS clients.
     *
     * This is intentionally bounded so that a large number
     * of connections cannot create unlimited threads.
     */
    private ExecutorService clientExecutor;


    /**
     * Constructor injection.
     */
    public GpsSocketServer(
            SocketConfig socketConfig,
            LocationService locationService,
            ObjectMapper objectMapper
    ) {

        this.socketConfig =
                socketConfig;

        this.locationService =
                locationService;

        this.objectMapper =
                objectMapper;
    }


    /**
     * Start the TCP server after Spring Boot has initialized
     * the application context.
     */
    @PostConstruct
    public void startServer() {

        validateConfiguration();


        /*
         * Create a fixed-size worker pool.
         *
         * Every accepted GPS client gets one worker task.
         */
        clientExecutor =
                Executors.newFixedThreadPool(
                        socketConfig.getMaxConnections()
                );


        Thread serverThread =
                new Thread(
                        this::runServer,
                        "unishut-gps-tcp-server"
                );


        /*
         * The acceptor thread should not prevent graceful
         * Spring Boot shutdown.
         */
        serverThread.setDaemon(true);


        serverThread.start();


        logger.info(
                "UniShut GPS TCP server starting on {}:{}",
                socketConfig.getHost(),
                socketConfig.getPort()
        );
    }


    /**
     * Main TCP server loop.
     *
     * This thread only accepts connections.
     *
     * Actual client processing happens inside worker
     * threads from the ExecutorService.
     */
    private void runServer() {

        try {

            /*
             * Bind the server socket to the configured
             * host and port.
             */
            serverSocket =
                    new ServerSocket(
                            socketConfig.getPort()
                    );


            running.set(true);


            logger.info(
                    "UniShut GPS TCP server started successfully "
                            + "on {}:{}",
                    socketConfig.getHost(),
                    socketConfig.getPort()
            );


            while (running.get()) {

                try {

                    Socket clientSocket =
                            serverSocket.accept();


                    /*
                     * Configure the client connection.
                     */
                    clientSocket.setKeepAlive(
                            true
                    );


                    clientSocket.setTcpNoDelay(
                            true
                    );


                    /*
                     * Prevent a client from blocking its
                     * worker thread indefinitely.
                     */
                    clientSocket.setSoTimeout(
                            30_000
                    );


                    /*
                     * Submit the client to the worker pool.
                     *
                     * This is the actual multithreading
                     * implementation.
                     */
                    clientExecutor.execute(
                            () -> handleClient(
                                    clientSocket
                            )
                    );

                } catch (SocketException exception) {

                    /*
                     * Closing ServerSocket during shutdown
                     * normally produces SocketException.
                     */
                    if (running.get()) {

                        logger.error(
                                "GPS TCP server socket error",
                                exception
                        );
                    }

                } catch (IOException exception) {

                    if (running.get()) {

                        logger.error(
                                "Failed to accept GPS client",
                                exception
                        );
                    }
                }
            }

        } catch (IOException exception) {

            logger.error(
                    "Unable to start UniShut GPS TCP server "
                            + "on port {}",
                    socketConfig.getPort(),
                    exception
            );

        } finally {

            running.set(false);
        }
    }


    /**
     * Process one connected GPS client.
     *
     * This method runs inside a worker thread.
     */
    private void handleClient(
            Socket clientSocket
    ) {

        String clientAddress =
                clientSocket.getRemoteSocketAddress()
                        .toString();


        logger.info(
                "GPS client connected: {}",
                clientAddress
        );


        try (
                Socket socket = clientSocket;

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        );

                PrintWriter writer =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true,
                                StandardCharsets.UTF_8
                        )
        ) {

            String message;


            /*
             * One TCP connection may send multiple
             * newline-delimited GPS messages.
             */
            while (
                    running.get()
                            && (message =
                            reader.readLine()) != null
            ) {

                /*
                 * Ignore empty messages.
                 */
                if (message.isBlank()) {

                    continue;
                }


                /*
                 * Reject oversized messages before
                 * JSON deserialization.
                 */
                if (message.length()
                        > socketConfig.getMaxMessageSize()) {

                    sendError(
                            writer,
                            "GPS message is too large"
                    );


                    logger.warn(
                            "Rejected oversized GPS message from {}",
                            clientAddress
                    );


                    continue;
                }


                processMessage(
                        message,
                        writer,
                        clientAddress
                );
            }

        } catch (SocketTimeoutException exception) {

            logger.warn(
                    "GPS client timed out: {}",
                    clientAddress
            );

        } catch (IOException exception) {

            /*
             * GPS/mobile clients can disconnect unexpectedly.
             * This is not necessarily a server error.
             */
            logger.debug(
                    "GPS client disconnected: {}",
                    clientAddress
            );

        } catch (Exception exception) {

            logger.error(
                    "Unexpected error while processing GPS client {}",
                    clientAddress,
                    exception
            );

        } finally {

            logger.info(
                    "GPS client connection closed: {}",
                    clientAddress
            );
        }
    }


    /**
     * Deserialize one GPS JSON message and pass it
     * to LocationService.
     */
    private void processMessage(
            String message,
            PrintWriter writer,
            String clientAddress
    ) {

        try {

            BusLocationDTO locationDTO =
                    objectMapper.readValue(
                            message,
                            BusLocationDTO.class
                    );


            /*
             * LocationService performs:
             *
             * - validation
             * - bus lookup
             * - trip lookup
             * - persistence
             * - WebSocket broadcast
             */
            BusLocationDTO savedLocation =
                    locationService.processLocation(
                            locationDTO
                    );


            /*
             * Send acknowledgement to the GPS client.
             */
            writer.println(
                    objectMapper.writeValueAsString(
                            new TcpResponse(
                                    true,
                                    "LOCATION_RECEIVED",
                                    savedLocation
                            )
                    )
            );


            logger.debug(
                    "GPS location processed successfully from {}",
                    clientAddress
            );

        } catch (Exception exception) {

            logger.warn(
                    "Invalid GPS message from {}: {}",
                    clientAddress,
                    exception.getMessage()
            );


            sendError(
                    writer,
                    "Invalid GPS location data"
            );
        }
    }


    /**
     * Send an error response to a GPS client.
     */
    private void sendError(
            PrintWriter writer,
            String message
    ) {

        try {

            writer.println(
                    objectMapper.writeValueAsString(
                            new TcpResponse(
                                    false,
                                    message,
                                    null
                            )
                    )
            );

        } catch (Exception exception) {

            logger.debug(
                    "Unable to send TCP error response",
                    exception
            );
        }
    }


    /**
     * Validate networking configuration.
     */
    private void validateConfiguration() {

        if (socketConfig.getPort() < 1
                || socketConfig.getPort() > 65_535) {

            throw new IllegalStateException(
                    "Invalid GPS TCP port: "
                            + socketConfig.getPort()
            );
        }


        if (socketConfig.getMaxConnections() <= 0) {

            throw new IllegalStateException(
                    "GPS max-connections must be greater than zero"
            );
        }


        if (socketConfig.getMaxMessageSize() <= 0) {

            throw new IllegalStateException(
                    "GPS max-message-size must be greater than zero"
            );
        }
    }


    /**
     * Gracefully stop the TCP server when Spring Boot
     * shuts down.
     */
    @PreDestroy
    public void stopServer() {

        logger.info(
                "Stopping UniShut GPS TCP server..."
        );


        running.set(false);


        /*
         * Closing the ServerSocket releases the blocking
         * accept() operation.
         */
        if (serverSocket != null) {

            try {

                serverSocket.close();

            } catch (IOException exception) {

                logger.warn(
                        "Error while closing GPS TCP server socket",
                        exception
                );
            }
        }


        /*
         * Stop accepting new worker tasks.
         */
        if (clientExecutor != null) {

            clientExecutor.shutdown();


            try {

                if (!clientExecutor.awaitTermination(
                        5,
                        TimeUnit.SECONDS
                )) {

                    clientExecutor.shutdownNow();
                }

            } catch (InterruptedException exception) {

                clientExecutor.shutdownNow();

                Thread.currentThread().interrupt();
            }
        }


        logger.info(
                "UniShut GPS TCP server stopped"
        );
    }


    /**
     * Simple TCP response object.
     *
     * Used only by the TCP server.
     */
    private static class TcpResponse {

        private final boolean success;

        private final String message;

        private final BusLocationDTO location;


        private TcpResponse(
                boolean success,
                String message,
                BusLocationDTO location
        ) {

            this.success =
                    success;

            this.message =
                    message;

            this.location =
                    location;
        }


        public boolean isSuccess() {
            return success;
        }


        public String getMessage() {
            return message;
        }


        public BusLocationDTO getLocation() {
            return location;
        }
    }
}