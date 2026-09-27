package com.unishut.service;

import com.unishut.dto.TripDTO;
import com.unishut.exception.BadRequestException;
import com.unishut.exception.ResourceNotFoundException;
import com.unishut.model.Bus;
import com.unishut.model.Location;
import com.unishut.model.Route;
import com.unishut.model.Trip;
import com.unishut.repository.LocationRepository;
import com.unishut.repository.ReservationRepository;
import com.unishut.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.unishut.service.NotificationService;

/**
 * Service responsible for trip-related business logic.
 *
 * Responsibilities:
 *
 * - Create trips
 * - Update trips
 * - Delete trips
 * - Find trips
 * - Search upcoming trips
 * - Find active trips
 * - Find a bus's current trip
 * - Manage trip status
 * - Read reservation statistics
 * - Read latest GPS location
 * - Convert Trip entities into TripDTO objects
 */
@Service
@Transactional
public class TripService {

        private final TripRepository tripRepository;

        private final RouteService routeService;

        private final BusService busService;

        private final ReservationRepository reservationRepository;

        private final LocationRepository locationRepository;
        private final NotificationService notificationService;

        /**
         * Constructor injection.
         */
        public TripService(
                        TripRepository tripRepository,
                        RouteService routeService,
                        BusService busService,
                        ReservationRepository reservationRepository,
                        LocationRepository locationRepository,
                        NotificationService notificationService) {

                this.tripRepository = tripRepository;
                this.routeService = routeService;
                this.busService = busService;
                this.reservationRepository = reservationRepository;
                this.locationRepository = locationRepository;
                this.notificationService = notificationService;
        }

        /**
         * Create a new trip.
         *
         * @param tripDTO trip information
         * @return created trip
         */
        public TripDTO createTrip(
                        TripDTO tripDTO) {

                validateTripDTO(tripDTO);

                Route route = routeService.getRouteEntityById(
                                tripDTO.getRouteId());

                Bus bus = busService.getBusEntityById(
                                tripDTO.getBusId());

                /*
                 * A bus under maintenance cannot be assigned
                 * to a new trip.
                 */
                if ("MAINTENANCE".equalsIgnoreCase(
                                bus.getStatus())) {

                        throw new BadRequestException(
                                        "A bus under maintenance cannot be assigned to a trip");
                }

                /*
                 * Prevent the same bus from being scheduled
                 * for another active trip at approximately
                 * the same time.
                 */
                validateBusScheduleConflict(
                                bus,
                                tripDTO.getStartTime(),
                                null);

                Trip trip = new Trip();

                trip.setRoute(route);

                trip.setBus(bus);

                trip.setStartTime(
                                tripDTO.getStartTime());

                trip.setStatus(
                                normalizeStatus(
                                                tripDTO.getStatus()));

                Trip savedTrip = tripRepository.save(
                                trip);

                return toDTO(savedTrip);
        }

        /**
         * Get a trip by ID.
         *
         * @param id trip ID
         * @return trip DTO
         */
        @Transactional(readOnly = true)
        public TripDTO getTripById(
                        Long id) {

                return toDTO(
                                getTripEntityById(id));
        }

        /**
         * Get all trips.
         *
         * @return all trips
         */
        @Transactional(readOnly = true)
        public List<TripDTO> getAllTrips() {

                return tripRepository
                                .findAll()
                                .stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get trips by status.
         *
         * @param status trip status
         * @return matching trips
         */
        @Transactional(readOnly = true)
        public List<TripDTO> getTripsByStatus(
                        String status) {

                String normalizedStatus = normalizeStatus(status);

                return tripRepository
                                .findByStatusIgnoreCase(
                                                normalizedStatus)
                                .stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get upcoming trips.
         *
         * Results are ordered by start time.
         *
         * @return upcoming trips
         */
        @Transactional(readOnly = true)
        public List<TripDTO> getUpcomingTrips() {

                return tripRepository
                                .findByStartTimeAfterOrderByStartTimeAsc(
                                                LocalDateTime.now())
                                .stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get upcoming trips for a route.
         *
         * @param routeId route ID
         * @return upcoming trips
         */
        @Transactional(readOnly = true)
        public List<TripDTO> getUpcomingTripsByRoute(
                        Long routeId) {

                Route route = routeService.getRouteEntityById(
                                routeId);

                return tripRepository
                                .findByRouteAndStartTimeAfterOrderByStartTimeAsc(
                                                route,
                                                LocalDateTime.now())
                                .stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get upcoming trips for a bus.
         *
         * @param busId bus ID
         * @return upcoming trips
         */
        @Transactional(readOnly = true)
        public List<TripDTO> getUpcomingTripsByBus(
                        Long busId) {

                Bus bus = busService.getBusEntityById(
                                busId);

                return tripRepository
                                .findByBusAndStartTimeAfterOrderByStartTimeAsc(
                                                bus,
                                                LocalDateTime.now())
                                .stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get currently active trips.
         *
         * Active trips are:
         *
         * - BOARDING
         * - IN_PROGRESS
         *
         * @return active trips
         */
        @Transactional(readOnly = true)
        public List<TripDTO> getActiveTrips() {

                List<Trip> boardingTrips = tripRepository
                                .findByStatusIgnoreCaseOrderByStartTimeAsc(
                                                "BOARDING");

                List<Trip> inProgressTrips = tripRepository
                                .findByStatusIgnoreCaseOrderByStartTimeAsc(
                                                "IN_PROGRESS");

                return java.util.stream.Stream
                                .concat(
                                                boardingTrips.stream(),
                                                inProgressTrips.stream())
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Find the currently active trip for a bus.
         *
         * This method is used by LocationService when a GPS
         * packet does not explicitly contain a trip ID.
         *
         * Priority:
         *
         * 1. IN_PROGRESS
         * 2. BOARDING
         * 3. SCHEDULED shortly before departure
         *
         * @param bus bus entity
         * @return active trip, or null when none exists
         */
        @Transactional(readOnly = true)
        public Trip findActiveTripByBus(
                        Bus bus) {

                if (bus == null
                                || bus.getId() == null) {

                        throw new BadRequestException(
                                        "A valid bus is required");
                }

                /*
                 * First priority:
                 * IN_PROGRESS
                 */
                List<Trip> inProgressTrips = tripRepository
                                .findByBusAndStatusIgnoreCase(
                                                bus,
                                                "IN_PROGRESS");

                if (!inProgressTrips.isEmpty()) {

                        return selectEarliestTrip(
                                        inProgressTrips);
                }

                /*
                 * Second priority:
                 * BOARDING
                 */
                List<Trip> boardingTrips = tripRepository
                                .findByBusAndStatusIgnoreCase(
                                                bus,
                                                "BOARDING");

                if (!boardingTrips.isEmpty()) {

                        return selectEarliestTrip(
                                        boardingTrips);
                }

                /*
                 * Third priority:
                 * SCHEDULED trip that starts soon.
                 */
                List<Trip> scheduledTrips = tripRepository
                                .findByBusAndStatusIgnoreCase(
                                                bus,
                                                "SCHEDULED");

                LocalDateTime now = LocalDateTime.now();

                LocalDateTime futureLimit = now.plusMinutes(30);

                return scheduledTrips
                                .stream()
                                .filter(trip -> trip.getStartTime() != null)
                                .filter(trip -> !trip.getStartTime()
                                                .isBefore(now))
                                .filter(trip -> !trip.getStartTime()
                                                .isAfter(futureLimit))
                                .sorted(
                                                java.util.Comparator
                                                                .comparing(
                                                                                Trip::getStartTime))
                                .findFirst()
                                .orElse(null);
        }

        /**
         * Update an existing trip.
         *
         * @param id      trip ID
         * @param tripDTO updated trip data
         * @return updated trip
         */
        public TripDTO updateTrip(
                        Long id,
                        TripDTO tripDTO) {

                validateTripDTO(tripDTO);

                Trip trip = getTripEntityById(id);

                Route route = routeService.getRouteEntityById(
                                tripDTO.getRouteId());

                Bus bus = busService.getBusEntityById(
                                tripDTO.getBusId());

                /*
                 * A maintenance bus cannot be used.
                 */
                if ("MAINTENANCE".equalsIgnoreCase(
                                bus.getStatus())) {

                        throw new BadRequestException(
                                        "A bus under maintenance cannot be assigned to a trip");
                }

                validateBusScheduleConflict(
                                bus,
                                tripDTO.getStartTime(),
                                id);

                /*
                 * Validate status transition.
                 */
                String newStatus = normalizeStatus(
                                tripDTO.getStatus());

                validateStatusTransition(
                                trip.getStatus(),
                                newStatus);

                trip.setRoute(route);

                trip.setBus(bus);

                trip.setStartTime(
                                tripDTO.getStartTime());

                trip.setStatus(newStatus);

                return toDTO(
                                tripRepository.save(trip));
        }

        /**
         * Update only the status of a trip.
         *
         * @param id     trip ID
         * @param status new status
         * @return updated trip
         */
        public TripDTO updateTripStatus(
                        Long id,
                        String status) {

                Trip trip = getTripEntityById(id);

                String normalizedStatus = normalizeStatus(status);

                validateStatusTransition(
                                trip.getStatus(),
                                normalizedStatus);

                trip.setStatus(
                                normalizedStatus);

                Trip savedTrip = tripRepository.save(
                                trip);

                reservationRepository
                                .findByTripOrderByIssuedAtAsc(
                                                trip)
                                .forEach(reservation -> {

                                        notificationService.createTripNotification(
                                                        reservation.getUser().getId(),
                                                        "Your trip status changed to "
                                                                        + normalizedStatus
                                                                        + ".");

                                });

                return toDTO(
                                savedTrip);
        }

        /**
         * Delete a trip.
         *
         * Trips with active reservations cannot be deleted.
         * They should normally be cancelled instead.
         *
         * @param id trip ID
         */
        public void deleteTrip(
                        Long id) {

                Trip trip = getTripEntityById(id);

                long confirmedReservations = reservationRepository
                                .countByTripAndStatus(
                                                trip,
                                                "CONFIRMED");

                long waitlistedReservations = reservationRepository
                                .countByTripAndStatus(
                                                trip,
                                                "WAITLISTED");

                long boardedReservations = reservationRepository
                                .countByTripAndStatus(
                                                trip,
                                                "BOARDED");

                if (confirmedReservations > 0
                                || waitlistedReservations > 0
                                || boardedReservations > 0) {

                        throw new BadRequestException(
                                        "Trip cannot be deleted while reservations exist");
                }

                tripRepository.delete(trip);
        }

        /**
         * Retrieve the actual Trip entity.
         *
         * Other services can use this method when they need
         * relationships with the Trip entity.
         *
         * @param id trip ID
         * @return Trip entity
         */
        @Transactional(readOnly = true)
        public Trip getTripEntityById(
                        Long id) {

                if (id == null || id <= 0) {

                        throw new BadRequestException(
                                        "Trip ID must be a positive number");
                }

                return tripRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Trip not found with ID: "
                                                                + id));
        }

        /**
         * Convert a Trip entity into TripDTO.
         *
         * Includes:
         *
         * - Route information
         * - Bus information
         * - Reservation count
         * - Waitlist count
         * - Latest GPS coordinates
         *
         * @param trip trip entity
         * @return TripDTO
         */
        @Transactional(readOnly = true)
        public TripDTO toDTO(
                        Trip trip) {

                if (trip == null) {
                        return null;
                }

                Route route = trip.getRoute();

                Bus bus = trip.getBus();

                long confirmedCount = reservationRepository
                                .countByTripAndStatus(
                                                trip,
                                                "CONFIRMED");

                long waitlistedCount = reservationRepository
                                .countByTripAndStatus(
                                                trip,
                                                "WAITLISTED");

                /*
                 * Latest GPS location.
                 */
                /*
                 * Latest GPS location from assigned bus.
                 */
                Double latitude = null;

                Double longitude = null;

                if (bus != null) {

                        latitude = bus.getLatitude();

                        longitude = bus.getLongitude();

                }

                /*
                 * TripDTO currently uses Integer for reservation
                 * counters.
                 */
                Integer reserved = safeInteger(
                                confirmedCount);

                Integer waitlisted = safeInteger(
                                waitlistedCount);

                return new TripDTO(
                                trip.getId(),

                                route != null
                                                ? route.getId()
                                                : null,

                                bus != null
                                                ? bus.getId()
                                                : null,

                                route != null
                                                ? route.getName()
                                                : null,

                                route != null
                                                ? route.getOrigin()
                                                : null,

                                route != null
                                                ? route.getDestination()
                                                : null,

                                bus != null
                                                ? bus.getNumber()
                                                : null,

                                trip.getStartTime(),

                                trip.getStatus(),

                                reserved,

                                waitlisted,

                                null,

                                latitude,

                                longitude);
        }

        /**
         * Validate incoming TripDTO.
         */
        private void validateTripDTO(
                        TripDTO tripDTO) {

                if (tripDTO == null) {

                        throw new BadRequestException(
                                        "Trip information is required");
                }

                if (tripDTO.getRouteId() == null
                                || tripDTO.getRouteId() <= 0) {

                        throw new BadRequestException(
                                        "A valid route ID is required");
                }

                if (tripDTO.getBusId() == null
                                || tripDTO.getBusId() <= 0) {

                        throw new BadRequestException(
                                        "A valid bus ID is required");
                }

                if (tripDTO.getStartTime() == null) {

                        throw new BadRequestException(
                                        "Trip start time is required");
                }

                if (tripDTO.getStatus() == null
                                || tripDTO.getStatus().isBlank()) {

                        throw new BadRequestException(
                                        "Trip status is required");
                }

                /*
                 * A newly created/updated trip cannot be scheduled
                 * in the past.
                 */
                if (tripDTO.getStartTime()
                                .isBefore(LocalDateTime.now())) {

                        throw new BadRequestException(
                                        "Trip start time cannot be in the past");
                }

                normalizeStatus(
                                tripDTO.getStatus());
        }

        /**
         * Prevent a bus from being assigned to overlapping
         * active trips.
         *
         * A one-minute tolerance is used around the requested
         * start time to catch accidental duplicate schedules.
         */
        private void validateBusScheduleConflict(
                        Bus bus,
                        LocalDateTime startTime,
                        Long excludedTripId) {

                LocalDateTime windowStart = startTime.minusMinutes(1);

                LocalDateTime windowEnd = startTime.plusMinutes(1);

                List<Trip> existingTrips = tripRepository
                                .findByBusAndStartTimeAfterOrderByStartTimeAsc(
                                                bus,
                                                windowStart);

                for (Trip existingTrip : existingTrips) {

                        if (existingTrip.getStartTime() == null) {

                                continue;
                        }

                        /*
                         * We only care about trips inside the
                         * scheduling window.
                         */
                        if (existingTrip.getStartTime()
                                        .isAfter(windowEnd)) {

                                break;
                        }

                        /*
                         * Ignore the trip currently being updated.
                         */
                        if (excludedTripId != null
                                        && excludedTripId.equals(
                                                        existingTrip.getId())) {

                                continue;
                        }

                        String existingStatus = existingTrip.getStatus();

                        /*
                         * Completed and cancelled trips do not block
                         * another scheduled trip.
                         */
                        if ("CANCELLED".equalsIgnoreCase(
                                        existingStatus)
                                        || "COMPLETED".equalsIgnoreCase(
                                                        existingStatus)) {

                                continue;
                        }

                        throw new BadRequestException(
                                        "Bus already has another active trip "
                                                        + "scheduled around this time");
                }
        }

        /**
         * Validate allowed trip status values.
         */
        private String normalizeStatus(
                        String status) {

                if (status == null
                                || status.isBlank()) {

                        throw new BadRequestException(
                                        "Trip status is required");
                }

                String normalized = status.trim()
                                .toUpperCase(
                                                Locale.ROOT);

                if (!normalized.equals("SCHEDULED")
                                && !normalized.equals("BOARDING")
                                && !normalized.equals("IN_PROGRESS")
                                && !normalized.equals("COMPLETED")
                                && !normalized.equals("CANCELLED")) {

                        throw new BadRequestException(
                                        "Invalid trip status: "
                                                        + status);
                }

                return normalized;
        }

        /**
         * Validate sensible trip status transitions.
         *
         * Normal flow:
         *
         * SCHEDULED
         * ↓
         * BOARDING
         * ↓
         * IN_PROGRESS
         * ↓
         * COMPLETED
         *
         * Cancellation is allowed before completion.
         */
        private void validateStatusTransition(
                        String currentStatus,
                        String newStatus) {

                if (currentStatus == null
                                || currentStatus.isBlank()) {

                        return;
                }

                String current = currentStatus
                                .trim()
                                .toUpperCase(
                                                Locale.ROOT);

                /*
                 * No transition is required when the status
                 * is unchanged.
                 */
                if (current.equals(newStatus)) {

                        return;
                }

                /*
                 * Terminal states cannot be reopened.
                 */
                if ("CANCELLED".equals(current)) {

                        throw new BadRequestException(
                                        "A cancelled trip cannot be activated again");
                }

                if ("COMPLETED".equals(current)) {

                        throw new BadRequestException(
                                        "A completed trip cannot change status");
                }

                /*
                 * SCHEDULED → BOARDING or CANCELLED
                 */
                if ("SCHEDULED".equals(current)) {

                        if (!newStatus.equals("BOARDING")
                                        && !newStatus.equals("CANCELLED")) {

                                throw new BadRequestException(
                                                "Scheduled trip can only move to "
                                                                + "BOARDING or CANCELLED");
                        }

                        return;
                }

                /*
                 * BOARDING → IN_PROGRESS or CANCELLED
                 */
                if ("BOARDING".equals(current)) {

                        if (!newStatus.equals("IN_PROGRESS")
                                        && !newStatus.equals("CANCELLED")) {

                                throw new BadRequestException(
                                                "Boarding trip can only move to "
                                                                + "IN_PROGRESS or CANCELLED");
                        }

                        return;
                }

                /*
                 * IN_PROGRESS → COMPLETED or CANCELLED
                 */
                if ("IN_PROGRESS".equals(current)) {

                        if (!newStatus.equals("COMPLETED")
                                        && !newStatus.equals("CANCELLED")) {

                                throw new BadRequestException(
                                                "In-progress trip can only move to "
                                                                + "COMPLETED or CANCELLED");
                        }
                }
        }

        /**
         * Select the earliest trip from a list.
         */
        private Trip selectEarliestTrip(
                        List<Trip> trips) {

                return trips
                                .stream()
                                .filter(trip -> trip.getStartTime() != null)
                                .sorted(
                                                java.util.Comparator
                                                                .comparing(
                                                                                Trip::getStartTime))
                                .findFirst()
                                .orElse(
                                                trips.get(0));
        }

        /**
         * Safely convert a long count to Integer.
         */
        private Integer safeInteger(
                        long value) {

                if (value > Integer.MAX_VALUE) {

                        return Integer.MAX_VALUE;
                }

                return (int) value;
        }
}