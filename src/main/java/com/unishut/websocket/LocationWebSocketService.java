package com.unishut.websocket;

import com.unishut.dto.BusLocationDTO;
import com.unishut.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket service responsible for broadcasting
 * validated GPS location updates to connected clients.
 *
 * GPS flow:
 *
 * GPS Device
 *      |
 *      | TCP
 *      v
 * GpsSocketServer
 *      |
 *      v
 * LocationService
 *      |
 *      v
 * LocationWebSocketService
 *      |
 *      +----------------------+
 *      |                      |
 *      v                      v
 * /topic/buses/{busId}   /topic/trips/{tripId}
 *      |                      |
 *      +----------+-----------+
 *                 |
 *                 v
 *          React Live Map
 */
@Service
public class LocationWebSocketService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    LocationWebSocketService.class
            );


    private final SimpMessagingTemplate messagingTemplate;


    /**
     * Constructor injection.
     */
    public LocationWebSocketService(
            SimpMessagingTemplate messagingTemplate
    ) {

        this.messagingTemplate =
                messagingTemplate;
    }


    /**
     * Broadcast a saved Location entity.
     *
     * The same location is published to:
     *
     * /topic/buses/{busId}
     *
     * and, when a trip exists:
     *
     * /topic/trips/{tripId}
     *
     * @param location saved GPS location
     */
    public void broadcastLocation(
            Location location
    ) {

        if (location == null) {

            logger.warn(
                    "Cannot broadcast a null location"
            );

            return;
        }


        BusLocationDTO locationDTO =
                toDTO(location);


        /*
         * Broadcast to the bus-specific channel.
         */
        if (location.getBus() != null
                && location.getBus().getId() != null) {

            Long busId =
                    location.getBus().getId();


            String busDestination =
                    "/topic/buses/"
                            + busId;


            messagingTemplate.convertAndSend(
                    busDestination,
                    locationDTO
            );


            logger.debug(
                    "GPS location broadcast for bus {}",
                    busId
            );
        }


        /*
         * Broadcast to the trip-specific channel.
         */
        if (location.getTrip() != null
                && location.getTrip().getId() != null) {

            Long tripId =
                    location.getTrip().getId();


            String tripDestination =
                    "/topic/trips/"
                            + tripId;


            messagingTemplate.convertAndSend(
                    tripDestination,
                    locationDTO
            );


            logger.debug(
                    "GPS location broadcast for trip {}",
                    tripId
            );
        }


        /*
         * Also publish to a general bus-location channel.
         *
         * This is useful for an admin dashboard that wants
         * to observe multiple buses simultaneously.
         */
        messagingTemplate.convertAndSend(
                "/topic/buses",
                locationDTO
        );
    }


    /**
     * Broadcast a BusLocationDTO directly.
     *
     * This method is useful when a location has already
     * been converted into a DTO by another service.
     *
     * @param locationDTO GPS location DTO
     */
    public void broadcastLocation(
            BusLocationDTO locationDTO
    ) {

        if (locationDTO == null) {

            logger.warn(
                    "Cannot broadcast a null location DTO"
            );

            return;
        }


        /*
         * General bus channel.
         */
        messagingTemplate.convertAndSend(
                "/topic/buses",
                locationDTO
        );


        /*
         * Bus-specific channel.
         */
        if (locationDTO.getBusId() != null) {

            messagingTemplate.convertAndSend(
                    "/topic/buses/"
                            + locationDTO.getBusId(),
                    locationDTO
            );
        }


        /*
         * Trip-specific channel.
         */
        if (locationDTO.getTripId() != null) {

            messagingTemplate.convertAndSend(
                    "/topic/trips/"
                            + locationDTO.getTripId(),
                    locationDTO
            );
        }
    }


    /**
     * Convert Location entity into the transport DTO
     * used by WebSocket clients.
     *
     * @param location Location entity
     * @return BusLocationDTO
     */
    private BusLocationDTO toDTO(
            Location location
    ) {

        Long busId = null;

        Long tripId = null;


        if (location.getBus() != null) {

            busId =
                    location.getBus().getId();
        }


        if (location.getTrip() != null) {

            tripId =
                    location.getTrip().getId();
        }


        return new BusLocationDTO(
                busId,
                tripId,
                location.getLatitude(),
                location.getLongitude(),
                location.getRecordedAt(),
                null,
                "WEBSOCKET"
        );
    }
}