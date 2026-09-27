package com.unishut.repository;

import com.unishut.model.Reservation;
import com.unishut.model.Trip;
import com.unishut.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    /**
     * Find all reservations made by a particular user.
     *
     * Ordered from newest to oldest.
     */
    List<Reservation> findByUserOrderByIssuedAtDesc(
            User user
    );


    /**
     * Find all reservations belonging to a particular trip.
     */
    List<Reservation> findByTripOrderByIssuedAtAsc(
            Trip trip
    );


    /**
     * Find all reservations for a trip with a specific status.
     *
     * Examples:
     * CONFIRMED
     * WAITLISTED
     * BOARDED
     * CANCELLED
     */
    List<Reservation> findByTripAndStatus(
            Trip trip,
            String status
    );


    /**
     * Find a user's reservation for a particular trip.
     *
     * This is useful for preventing the same student
     * from booking the same trip multiple times.
     */
    Optional<Reservation> findByUserAndTrip(
            User user,
            Trip trip
    );


    /**
     * Check whether a user already has a reservation
     * for a particular trip.
     */
    boolean existsByUserAndTrip(
            User user,
            Trip trip
    );


    /**
     * Find a reservation using its QR value.
     *
     * Used during QR boarding verification.
     */
    Optional<Reservation> findByQr(
            String qr
    );


    /**
     * Check whether a QR value already exists.
     */
    boolean existsByQr(
            String qr
    );


    /**
     * Find a reservation using a particular seat on a trip.
     *
     * This is important for preventing two students
     * from receiving the same seat.
     */
    Optional<Reservation> findByTripAndSeatNumber(
            Trip trip,
            String seatNumber
    );


    /**
     * Check whether a particular seat is already reserved
     * for a trip.
     */
    boolean existsByTripAndSeatNumber(
            Trip trip,
            String seatNumber
    );


    /**
     * Find all active reservations for a trip.
     *
     * CANCELLED reservations are excluded.
     */
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.trip = :trip
            AND r.status <> 'CANCELLED'
            ORDER BY r.issuedAt ASC
            """)
    List<Reservation> findActiveReservationsByTrip(
            @Param("trip") Trip trip
    );


    /**
     * Find confirmed reservations for a trip.
     */
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.trip = :trip
            AND r.status = 'CONFIRMED'
            ORDER BY r.serialNumber ASC
            """)
    List<Reservation> findConfirmedReservationsByTrip(
            @Param("trip") Trip trip
    );


    /**
     * Find waitlisted reservations ordered by queue position.
     */
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.trip = :trip
            AND r.status = 'WAITLISTED'
            ORDER BY r.queuePosition ASC
            """)
    List<Reservation> findWaitlistedReservationsByTrip(
            @Param("trip") Trip trip
    );


    /**
     * Count confirmed reservations for a trip.
     */
    long countByTripAndStatus(
            Trip trip,
            String status
    );


    /**
     * Count all waitlisted reservations for a trip.
     */
    long countByTripAndStatusAndQueuePositionIsNotNull(
            Trip trip,
            String status
    );


    /**
     * Find the first waitlisted reservation.
     *
     * This is used when a confirmed reservation is cancelled
     * and the next student needs to be promoted.
     */
    Optional<Reservation> findFirstByTripAndStatusOrderByQueuePositionAsc(
            Trip trip,
            String status
    );


    /**
     * Pessimistic database lock for a trip's reservations.
     *
     * This is intentionally used by the reservation service
     * when processing concurrent seat requests.
     *
     * The lock prevents two concurrent transactions from
     * modifying the same reservation set at the same time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.trip = :trip
            AND r.status <> 'CANCELLED'
            ORDER BY r.serialNumber ASC
            """)
    List<Reservation> findActiveReservationsForUpdate(
            @Param("trip") Trip trip
    );


    /**
     * Find a specific reservation and lock it for update.
     *
     * Useful when cancelling or boarding a reservation
     * while another operation may be processing it.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.id = :id
            """)
    Optional<Reservation> findByIdForUpdate(
            @Param("id") Long id
    );
}