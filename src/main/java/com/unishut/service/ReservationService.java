package com.unishut.service;

import com.unishut.dto.ReservationDTO;
import com.unishut.exception.BadRequestException;
import com.unishut.exception.ResourceNotFoundException;
import com.unishut.model.Bus;
import com.unishut.model.Reservation;
import com.unishut.model.Route;
import com.unishut.model.Trip;
import com.unishut.model.User;
import com.unishut.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.unishut.service.NotificationService;

/**
 * Service responsible for reservation business logic.
 *
 * Main responsibilities:
 *
 * - Create reservations
 * - Allocate seats
 * - Handle waitlists
 * - Prevent duplicate reservations
 * - Cancel reservations
 * - Promote waitlisted students
 * - Generate boarding QR values
 * - Mark students as boarded
 * - Convert Reservation entities into DTOs
 *
 * Concurrency:
 *
 * Reservation creation uses SERIALIZABLE transaction isolation
 * together with the repository's pessimistic locking query.
 *
 * This is important because multiple student requests can arrive
 * at almost exactly the same time.
 */
@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final UserService userService;

    private final TripService tripService;
    private final NotificationService notificationService;


    /**
     * Constructor injection.
     */
    public ReservationService(
        ReservationRepository reservationRepository,
        UserService userService,
        TripService tripService,
        NotificationService notificationService
) {

    this.reservationRepository = reservationRepository;
    this.userService = userService;
    this.tripService = tripService;
    this.notificationService = notificationService;
}


    /**
     * Create a reservation for a student.
     *
     * The operation is executed using SERIALIZABLE isolation
     * to reduce the possibility of two concurrent booking
     * requests allocating the same seat.
     *
     * @param userId student ID
     * @param tripId trip ID
     * @return created reservation
     */
    @Transactional(
            isolation = Isolation.SERIALIZABLE
    )
    public ReservationDTO createReservation(
            Long userId,
            Long tripId
    ) {

        validatePositiveId(
                userId,
                "User ID"
        );

        validatePositiveId(
                tripId,
                "Trip ID"
        );


        User user =
                userService.getUserEntityById(
                        userId
                );


        /*
         * Only students can create student reservations.
         */
        if (!"STUDENT".equalsIgnoreCase(
                user.getRole()
        )) {

            throw new BadRequestException(
                    "Only students can create reservations"
            );
        }


        if (!user.isEnabled()) {

            throw new BadRequestException(
                    "User account is disabled"
            );
        }


        Trip trip =
                tripService.getTripEntityById(
                        tripId
                );


        validateTripForReservation(trip);


        /*
         * Prevent the same student from reserving
         * the same trip more than once.
         */
        if (reservationRepository
                .existsByUserAndTrip(
                        user,
                        trip
                )) {

            throw new BadRequestException(
                    "You already have a reservation for this trip"
            );
        }


        /*
         * Lock the current active reservations for this trip.
         *
         * The transaction isolation level additionally protects
         * the booking operation against concurrent transactions.
         */
        List<Reservation> activeReservations =
                reservationRepository
                        .findActiveReservationsForUpdate(
                                trip
                        );


        Bus bus = trip.getBus();


        if (bus == null) {

            throw new BadRequestException(
                    "No bus is assigned to this trip"
            );
        }


        Integer capacity =
                bus.getCapacity();


        if (capacity == null || capacity <= 0) {

            throw new BadRequestException(
                    "Trip bus has an invalid seating capacity"
            );
        }


        /*
         * Find the first available seat.
         */
        Integer availableSeat =
                findAvailableSeat(
                        activeReservations,
                        capacity
                );


        Reservation reservation =
                new Reservation();


        reservation.setUser(user);

        reservation.setTrip(trip);

        reservation.setIssuedAt(
                LocalDateTime.now()
        );


        if (availableSeat != null) {

            /*
             * A seat is available.
             */
            reservation.setSerialNumber(
                    availableSeat
            );

            reservation.setSeatNumber(
                    buildSeatNumber(
                            availableSeat
                    )
            );

            reservation.setQueuePosition(
                    null
            );

            reservation.setStatus(
                    "CONFIRMED"
            );

            reservation.setQr(
                    generateQr(
                            trip,
                            user
                    )
            );

        } else {

            /*
             * No seat is available.
             *
             * Student goes to the waiting list.
             */
            int nextQueuePosition =
                    getNextQueuePosition(
                            activeReservations
                    );

            reservation.setSerialNumber(
                    null
            );

            reservation.setSeatNumber(
                    null
            );

            reservation.setQueuePosition(
                    nextQueuePosition
            );

            reservation.setStatus(
                    "WAITLISTED"
            );

            reservation.setQr(
                    null
            );
        }


        Reservation savedReservation =
                reservationRepository.save(
                        reservation
                );
        
        notificationService.createReservationNotification(
        userId,
        "Your bus reservation has been confirmed."
);


        return toDTO(
                savedReservation
        );
    }


    /**
     * Get a reservation by ID.
     *
     * @param id reservation ID
     * @return reservation DTO
     */
    @Transactional(readOnly = true)
    public ReservationDTO getReservationById(
            Long id
    ) {

        validatePositiveId(
                id,
                "Reservation ID"
        );


        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found with ID: "
                                                + id
                                )
                        );


        return toDTO(reservation);
    }


    /**
     * Get all reservations.
     *
     * Normally this method should be restricted to administrators.
     *
     * @return all reservations
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> getAllReservations() {

        return reservationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Get reservations belonging to a user.
     *
     * @param userId user ID
     * @return user's reservations
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByUser(
            Long userId
    ) {

        validatePositiveId(
                userId,
                "User ID"
        );


        User user =
                userService.getUserEntityById(
                        userId
                );


        return reservationRepository
                .findByUserOrderByIssuedAtDesc(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Get reservations for a trip.
     *
     * @param tripId trip ID
     * @return reservations for the trip
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByTrip(
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


        return reservationRepository
                .findByTripOrderByIssuedAtAsc(trip)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Get the current user's reservation for a specific trip.
     *
     * @param userId user ID
     * @param tripId trip ID
     * @return reservation
     */
    @Transactional(readOnly = true)
    public ReservationDTO getUserTripReservation(
            Long userId,
            Long tripId
    ) {

        User user =
                userService.getUserEntityById(
                        userId
                );


        Trip trip =
                tripService.getTripEntityById(
                        tripId
                );


        Reservation reservation =
                reservationRepository
                        .findByUserAndTrip(
                                user,
                                trip
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No reservation found for this user and trip"
                                )
                        );


        return toDTO(reservation);
    }


    /**
     * Cancel a reservation.
     *
     * If a confirmed reservation is cancelled, the first
     * waitlisted student is promoted automatically.
     *
     * The entire operation runs inside one transaction.
     *
     * @param reservationId reservation ID
     * @return cancelled reservation
     */
    @Transactional(
            isolation = Isolation.SERIALIZABLE
    )
    public ReservationDTO cancelReservation(
            Long reservationId
    ) {

        validatePositiveId(
                reservationId,
                "Reservation ID"
        );


        Reservation reservation =
                reservationRepository
                        .findByIdForUpdate(
                                reservationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found with ID: "
                                                + reservationId
                                )
                        );


        if ("CANCELLED".equalsIgnoreCase(
                reservation.getStatus()
        )) {

            throw new BadRequestException(
                    "Reservation is already cancelled"
            );
        }


        if ("BOARDED".equalsIgnoreCase(
                reservation.getStatus()
        )) {

            throw new BadRequestException(
                    "A boarded reservation cannot be cancelled"
            );
        }


        Trip trip =
                reservation.getTrip();


        boolean wasConfirmed =
                "CONFIRMED".equalsIgnoreCase(
                        reservation.getStatus()
                );


        /*
         * Cancel the reservation first.
         */
        reservation.setStatus(
                "CANCELLED"
        );

        reservation.setSerialNumber(
                null
        );

        reservation.setSeatNumber(
                null
        );

        reservation.setQueuePosition(
                null
        );

        reservation.setQr(
                null
        );


        Reservation cancelled =
                reservationRepository.save(
                        reservation
                );


        /*
         * If the cancelled reservation had a confirmed seat,
         * promote the first waiting student.
         */
        if (wasConfirmed) {

            promoteNextWaitlistedStudent(
                    trip
            );
        }


        return toDTO(cancelled);
    }


    /**
     * Promote the first waitlisted reservation.
     *
     * The method expects to be called inside a transaction.
     */
    private void promoteNextWaitlistedStudent(
            Trip trip
    ) {

        /*
         * Lock the active reservations before selecting
         * the next waitlisted student.
         */
        List<Reservation> activeReservations =
                reservationRepository
                        .findActiveReservationsForUpdate(
                                trip
                        );


        Bus bus =
                trip.getBus();


        if (bus == null
                || bus.getCapacity() == null
                || bus.getCapacity() <= 0) {

            return;
        }


        Integer availableSeat =
                findAvailableSeat(
                        activeReservations,
                        bus.getCapacity()
                );


        if (availableSeat == null) {

            return;
        }


        Reservation next =
                reservationRepository
                        .findFirstByTripAndStatusOrderByQueuePositionAsc(
                                trip,
                                "WAITLISTED"
                        )
                        .orElse(null);


        if (next == null) {

            return;
        }


        next.setStatus(
                "CONFIRMED"
        );

        next.setSerialNumber(
                availableSeat
        );

        next.setSeatNumber(
                buildSeatNumber(
                        availableSeat
                )
        );

        next.setQueuePosition(
                null
        );

        next.setQr(
                generateQr(
                        trip,
                        next.getUser()
                )
        );


        reservationRepository.save(next);


        /*
         * Recalculate remaining queue positions so that:
         *
         * 1, 2, 3...
         *
         * remain sequential.
         */
        normalizeWaitlistPositions(
                trip
        );
    }


    /**
     * Mark a confirmed reservation as boarded.
     *
     * QR validation is performed before changing the status.
     *
     * @param qr QR value
     * @return boarded reservation
     */
    public ReservationDTO boardByQr(
            String qr
    ) {

        if (qr == null || qr.isBlank()) {

            throw new BadRequestException(
                    "QR value is required"
            );
        }


        Reservation reservation =
                reservationRepository
                        .findByQr(
                                qr.trim()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation for this QR code was not found"
                                )
                        );


        if (!"CONFIRMED".equalsIgnoreCase(
                reservation.getStatus()
        )) {

            throw new BadRequestException(
                    "Only confirmed reservations can be boarded"
            );
        }


        Trip trip =
                reservation.getTrip();


        if (trip == null) {

            throw new BadRequestException(
                    "Reservation is not associated with a trip"
            );
        }


        if (!"BOARDING".equalsIgnoreCase(
                trip.getStatus()
        )
                && !"IN_PROGRESS".equalsIgnoreCase(
                trip.getStatus()
        )) {

            throw new BadRequestException(
                    "Boarding is not currently open for this trip"
            );
        }


        reservation.setStatus(
                "BOARDED"
        );


        return toDTO(
                reservationRepository.save(
                        reservation
                )
        );
    }


    /**
     * Find the first available seat.
     *
     * Seat numbers are:
     *
     * S-1
     * S-2
     * S-3
     * ...
     */
    private Integer findAvailableSeat(
            List<Reservation> activeReservations,
            int capacity
    ) {

        Set<Integer> occupiedSeats =
                new HashSet<>();


        for (Reservation reservation :
                activeReservations) {

            if (!"CANCELLED".equalsIgnoreCase(
                    reservation.getStatus()
            )
                    && reservation.getSerialNumber() != null) {

                occupiedSeats.add(
                        reservation.getSerialNumber()
                );
            }
        }


        for (int seat = 1;
             seat <= capacity;
             seat++) {

            if (!occupiedSeats.contains(seat)) {

                return seat;
            }
        }


        return null;
    }


    /**
     * Determine the next waiting-list position.
     */
    private int getNextQueuePosition(
            List<Reservation> activeReservations
    ) {

        int maxPosition = 0;


        for (Reservation reservation :
                activeReservations) {

            if ("WAITLISTED".equalsIgnoreCase(
                    reservation.getStatus()
            )
                    && reservation.getQueuePosition() != null) {

                maxPosition =
                        Math.max(
                                maxPosition,
                                reservation.getQueuePosition()
                        );
            }
        }


        return maxPosition + 1;
    }


    /**
     * Recalculate waiting-list positions.
     *
     * This keeps the queue sequential after promotion.
     */
    private void normalizeWaitlistPositions(
            Trip trip
    ) {

        List<Reservation> waitlisted =
                reservationRepository
                        .findWaitlistedReservationsByTrip(
                                trip
                        );


        int position = 1;


        for (Reservation reservation :
                waitlisted) {

            reservation.setQueuePosition(
                    position++
            );

            reservationRepository.save(
                    reservation
            );
        }
    }


    /**
     * Generate a unique QR value for a reservation.
     *
     * UUID is used to make collisions extremely unlikely.
     */
    private String generateQr(
            Trip trip,
            User user
    ) {

        String qr;

        do {

            qr =
                    "UNISHUT-"
                            + trip.getId()
                            + "-"
                            + user.getId()
                            + "-"
                            + UUID.randomUUID();

        } while (
                reservationRepository.existsByQr(qr)
        );


        return qr;
    }


    /**
     * Validate whether a trip can accept reservations.
     */
    private void validateTripForReservation(
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
                    "Cannot reserve a cancelled trip"
            );
        }


        if ("COMPLETED".equalsIgnoreCase(
                status
        )) {

            throw new BadRequestException(
                    "Cannot reserve a completed trip"
            );
        }


        if (!"SCHEDULED".equalsIgnoreCase(
                status
        )
                && !"BOARDING".equalsIgnoreCase(
                status
        )) {

            throw new BadRequestException(
                    "Reservations are not currently available for this trip"
            );
        }


        if (trip.getBus() == null) {

            throw new BadRequestException(
                    "No bus is assigned to this trip"
            );
        }
    }


    /**
     * Convert Reservation entity into ReservationDTO.
     */
    public ReservationDTO toDTO(
            Reservation reservation
    ) {

        if (reservation == null) {
            return null;
        }


        User user =
                reservation.getUser();

        Trip trip =
                reservation.getTrip();


        String userName = null;

        String routeName = null;

        String busNumber = null;


        if (user != null) {

            userName =
                    user.getName();
        }


        if (trip != null) {

            Route route =
                    trip.getRoute();

            Bus bus =
                    trip.getBus();


            if (route != null) {

                routeName =
                        route.getName();
            }


            if (bus != null) {

                busNumber =
                        bus.getNumber();
            }
        }


        return new ReservationDTO(
                reservation.getId(),

                user != null
                        ? user.getId()
                        : null,

                trip != null
                        ? trip.getId()
                        : null,

                userName,

                routeName,

                busNumber,

                reservation.getSerialNumber(),

                reservation.getSeatNumber(),

                reservation.getQueuePosition(),

                reservation.getStatus(),

                reservation.getIssuedAt(),

                reservation.getQr()
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


    /**
     * Build the frontend-friendly seat number.
     */
    private String buildSeatNumber(
            Integer serialNumber
    ) {

        return "S-" + serialNumber;
    }
}