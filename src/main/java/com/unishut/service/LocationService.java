package com.unishut.service;

import com.unishut.dto.BusLocationDTO;
import com.unishut.exception.BadRequestException;
import com.unishut.exception.ResourceNotFoundException;
import com.unishut.model.Bus;
import com.unishut.model.Location;
import com.unishut.model.Trip;
import com.unishut.repository.LocationRepository;
import com.unishut.websocket.LocationWebSocketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for GPS/location business logic.
 *
 * Responsibilities:
 *
 * - Validate incoming GPS coordinates
 * - Validate GPS timestamps
 * - Validate bus/trip relationships
 * - Save GPS locations
 * - Find latest bus location
 * - Find latest trip location
 * - Read location history
 * - Check GPS freshness
 * - Broadcast validated GPS updates through WebSocket
 *
 * GPS architecture:
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
 *      +------> MySQL
 *      |
 *      +------> LocationWebSocketService
 *                       |
 *                       v
 *                  React Live Map
 */
@Service
@Transactional
public class LocationService {

    /**
     * Maximum age allowed for an incoming GPS update.
     */
    private static final long MAX_LOCATION_AGE_MINUTES = 5;


    /**
     * Maximum future tolerance for a GPS timestamp.
     */
    private static final long MAX_FUTURE_MINUTES = 2;


    /**
     * Valid latitude range.
     */
    private static final double MIN_LATITUDE = -90.0;

    private static final double MAX_LATITUDE = 90.0;


    /**
     * Valid longitude range.
     */
    private static final double MIN_LONGITUDE = -180.0;

    private static final double MAX_LONGITUDE = 180.0;


    private final LocationRepository locationRepository;

    private final BusService busService;

    private final TripService tripService;

    private final LocationWebSocketService locationWebSocketService;


    /**
     * Constructor injection.
     */
    public LocationService(
            LocationRepository locationRepository,
            BusService busService,
            TripService tripService,
            LocationWebSocketService locationWebSocketService
    ) {
        this.locationRepository =
                locationRepository;

        this.busService =
                busService;

        this.tripService =
                tripService;

        this.locationWebSocketService =
                locationWebSocketService;
    }


    /**
     * Process an incoming GPS update.
     *
     * This is the main entry point used by the TCP
     * networking layer.
     *
     * Flow:
     *
     * BusLocationDTO
     *      ↓
     * Validation
     *      ↓
     * Bus lookup
     *      ↓
     * Trip validation
     *      ↓
     * Location persistence
     *      ↓
     * WebSocket broadcast
     *
     * @param locationDTO incoming GPS information
     * @return saved Location entity
     */
    public Location processLocationUpdate(
            BusLocationDTO locationDTO
    ) {

        validateLocationDTO(
                locationDTO
        );


        Bus bus =
                busService.getBusEntityById(
                        locationDTO.getBusId()
                );


        Trip trip = null;


        /*
         * If the GPS packet contains a trip ID,
         * verify that the trip actually belongs to
         * the same bus.
         */
        if (locationDTO.getTripId() != null) {

            trip =
                    tripService.getTripEntityById(
                            locationDTO.getTripId()
                    );


            if (trip.getBus() == null
                    || trip.getBus().getId() == null
                    || !trip.getBus()
                    .getId()
                    .equals(bus.getId())) {

                throw new BadRequestException(
                        "GPS bus does not match the trip bus"
                );
            }


            validateTripForLocation(
                    trip
            );
        }


        /*
         * If no trip ID was sent by the GPS device,
         * automatically identify the active trip for
         * this bus.
         */
        if (trip == null) {

            trip =
                    tripService.findActiveTripByBus(
                            bus
                    );
        }


        /*
         * Create the Location entity.
         */
        Location location =
                new Location();


        location.setBus(
                bus
        );


        location.setTrip(
                trip
        );


        location.setLatitude(
                locationDTO.getLatitude()
        );


        location.setLongitude(
                locationDTO.getLongitude()
        );


        location.setRecordedAt(
                locationDTO.getTimestamp()
        );


        /*
         * Persist the GPS location.
         */
        Location savedLocation =
                locationRepository.save(
                        location
                );


        /*
         * Update the current coordinates of the trip.
         *
         * This allows the dashboard to obtain the latest
         * coordinates without scanning the entire location
         * history.
         */
        if (trip != null) {

            updateTripCurrentLocation(
                    trip,
                    locationDTO.getLatitude(),
                    locationDTO.getLongitude()
            );
        }


        /*
         * Broadcast only AFTER the location has successfully
         * been persisted.
         *
         * This prevents the frontend from receiving a location
         * that failed to save to the database.
         */
        locationWebSocketService.broadcastLocation(
                savedLocation
        );


        return savedLocation;
    }


    /**
     * Process a GPS update and return a DTO.
     *
     * Used by the TCP server.
     *
     * @param locationDTO incoming GPS information
     * @return processed GPS location
     */
    public BusLocationDTO processLocation(
            BusLocationDTO locationDTO
    ) {

        Location savedLocation =
                processLocationUpdate(
                        locationDTO
                );


        return toDTO(
                savedLocation
        );
    }


    /**
     * Get the latest GPS location for a bus.
     *
     * @param busId bus ID
     * @return latest location
     */
    @Transactional(readOnly = true)
    public BusLocationDTO getLatestBusLocation(
            Long busId
    ) {

        validatePositiveId(
                busId,
                "Bus ID"
        );


        Bus bus =
                busService.getBusEntityById(
                        busId
                );


        Location location =
                locationRepository
                        .findFirstByBusOrderByRecordedAtDesc(
                                bus
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No GPS location found for bus ID: "
                                                + busId
                                )
                        );


        return toDTO(
                location
        );
    }


    /**
     * Get the latest GPS location for a trip.
     *
     * @param tripId trip ID
     * @return latest location
     */
    @Transactional(readOnly = true)
    public BusLocationDTO getLatestTripLocation(
            Long tripId
    ) {

        validatePositiveId(
                tripId,
                "Trip ID"
        );


        Trip trip =
                tripService.getTripEntityById(
                        tripId
                );


        Location location =
                locationRepository
                        .findFirstByTripOrderByRecordedAtDesc(
                                trip
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No GPS location found for trip ID: "
                                                + tripId
                                )
                        );


        return toDTO(
                location
        );
    }


    /**
     * Get the latest GPS location for a specific
     * bus and trip combination.
     *
     * @param busId bus ID
     * @param tripId trip ID
     * @return latest location
     */
    @Transactional(readOnly = true)
    public BusLocationDTO getLatestBusTripLocation(
            Long busId,
            Long tripId
    ) {

        validatePositiveId(
                busId,
                "Bus ID"
        );


        validatePositiveId(
                tripId,
                "Trip ID"
        );


        Bus bus =
                busService.getBusEntityById(
                        busId
                );


        Trip trip =
                tripService.getTripEntityById(
                        tripId
                );


        if (trip.getBus() == null
                || trip.getBus().getId() == null
                || !trip.getBus()
                .getId()
                .equals(bus.getId())) {

            throw new BadRequestException(
                    "Bus does not belong to the requested trip"
            );
        }


        Location location =
                locationRepository
                        .findFirstByBusAndTripOrderByRecordedAtDesc(
                                bus,
                                trip
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No GPS location found for this bus and trip"
                                )
                        );


        return toDTO(
                location
        );
    }


    /**
     * Get recent location history for a bus.
     *
     * @param busId bus ID
     * @param limit maximum number of records
     * @return recent locations
     */
    @Transactional(readOnly = true)
    public List<BusLocationDTO> getBusLocationHistory(
            Long busId,
            int limit
    ) {

        validatePositiveId(
                busId,
                "Bus ID"
        );


        if (limit <= 0) {

            throw new BadRequestException(
                    "History limit must be greater than zero"
            );
        }


        /*
         * Protect the database from excessively large
         * history requests.
         */
        int safeLimit =
                Math.min(
                        limit,
                        500
                );


        Bus bus =
                busService.getBusEntityById(
                        busId
                );


        List<Location> locations =
                locationRepository
                        .findRecentLocationsByBus(
                                bus
                        );


        return locations
                .stream()
                .limit(safeLimit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Determine whether the latest location of a bus
     * is still fresh.
     *
     * @param busId bus ID
     * @return true when a recent location exists
     */
    @Transactional(readOnly = true)
    public boolean isLocationFresh(
            Long busId
    ) {

        if (busId == null
                || busId <= 0) {

            return false;
        }


        Bus bus =
                busService.getBusEntityById(
                        busId
                );


        Location latest =
                locationRepository
                        .findFirstByBusOrderByRecordedAtDesc(
                                bus
                        )
                        .orElse(null);


        if (latest == null
                || latest.getRecordedAt() == null) {

            return false;
        }


        Duration age =
                Duration.between(
                        latest.getRecordedAt(),
                        LocalDateTime.now()
                );


        /*
         * A timestamp in the future is not considered
         * a valid fresh GPS reading.
         */
        if (age.isNegative()) {

            return false;
        }


        return age.toMinutes()
                <= MAX_LOCATION_AGE_MINUTES;
    }


    /**
     * Update the current GPS coordinates on the trip.
     *
     * Location remains the historical record while
     * Trip stores the latest known position.
     */
    private void updateTripCurrentLocation(
            Trip trip,
            Double latitude,
            Double longitude
    ) {

        trip.setLatitude(
                latitude
        );


        trip.setLongitude(
                longitude
        );
    }


    /**
     * Validate a trip before accepting GPS data.
     */
    private void validateTripForLocation(
            Trip trip
    ) {

        if (trip == null) {

            throw new BadRequestException(
                    "Trip is required"
            );
        }


        String status =
                trip.getStatus();


        if ("CANCELLED".equalsIgnoreCase(
                status
        )) {

            throw new BadRequestException(
                    "Cannot accept GPS data for a cancelled trip"
            );
        }


        if ("COMPLETED".equalsIgnoreCase(
                status
        )) {

            throw new BadRequestException(
                    "Cannot accept GPS data for a completed trip"
            );
        }


        if (!"SCHEDULED".equalsIgnoreCase(
                status
        )
                && !"BOARDING".equalsIgnoreCase(
                status
        )
                && !"IN_PROGRESS".equalsIgnoreCase(
                status
        )) {

            throw new BadRequestException(
                    "Trip is not in a valid GPS tracking state"
            );
        }
    }


    /**
     * Validate incoming GPS information.
     */
    private void validateLocationDTO(
            BusLocationDTO locationDTO
    ) {

        if (locationDTO == null) {

            throw new BadRequestException(
                    "Location information is required"
            );
        }


        if (locationDTO.getBusId() == null
                || locationDTO.getBusId() <= 0) {

            throw new BadRequestException(
                    "A valid bus ID is required"
            );
        }


        if (locationDTO.getLatitude() == null) {

            throw new BadRequestException(
                    "Latitude is required"
            );
        }


        if (locationDTO.getLongitude() == null) {

            throw new BadRequestException(
                    "Longitude is required"
            );
        }


        double latitude =
                locationDTO.getLatitude();


        double longitude =
                locationDTO.getLongitude();


        if (latitude < MIN_LATITUDE
                || latitude > MAX_LATITUDE) {

            throw new BadRequestException(
                    "Latitude must be between -90 and 90"
            );
        }


        if (longitude < MIN_LONGITUDE
                || longitude > MAX_LONGITUDE) {

            throw new BadRequestException(
                    "Longitude must be between -180 and 180"
            );
        }


        if (locationDTO.getTimestamp() == null) {

            throw new BadRequestException(
                    "GPS timestamp is required"
            );
        }


        LocalDateTime timestamp =
                locationDTO.getTimestamp();


        LocalDateTime now =
                LocalDateTime.now();


        /*
         * Reject timestamps that are too far in the future.
         */
        if (timestamp.isAfter(
                now.plusMinutes(
                        MAX_FUTURE_MINUTES
                )
        )) {

            throw new BadRequestException(
                    "GPS timestamp is too far in the future"
            );
        }


        /*
         * Reject stale GPS packets.
         */
        if (timestamp.isBefore(
                now.minusMinutes(
                        MAX_LOCATION_AGE_MINUTES
                )
        )) {

            throw new BadRequestException(
                    "GPS location is too old"
            );
        }


        /*
         * Source is optional, but an explicitly supplied
         * source cannot be blank.
         */
        if (locationDTO.getSource() != null
                && locationDTO.getSource().isBlank()) {

            throw new BadRequestException(
                    "GPS source cannot be blank"
            );
        }
    }


    /**
     * Convert Location entity to BusLocationDTO.
     */
    public BusLocationDTO toDTO(
            Location location
    ) {

        if (location == null) {

            return null;
        }


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
                "SERVER"
        );
    }


    /**
     * Validate a positive database ID.
     */
    private void validatePositiveId(
            Long id,
            String fieldName
    ) {

        if (id == null || id <= 0) {

            throw new BadRequestException(
                    fieldName
                            + " must be a positive number"
            );
        }
    }
}