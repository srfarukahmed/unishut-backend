package com.unishut.controller;

import com.unishut.dto.ReservationDTO;
import com.unishut.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    /**
     * Create reservation for a student.
     *
     * POST /api/reservations?userId=2&tripId=1
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ReservationDTO> createReservation(
            @RequestParam Long userId,
            @RequestParam Long tripId
    ) {

        ReservationDTO reservation =
                reservationService.createReservation(
                        userId,
                        tripId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservation);
    }


    /**
     * Get all reservations.
     *
     * ADMIN only.
     *
     * GET /api/reservations
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {

        return ResponseEntity.ok(
                reservationService.getAllReservations()
        );
    }


    /**
     * Get reservation by ID.
     *
     * GET /api/reservations/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<ReservationDTO> getReservationById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                reservationService.getReservationById(id)
        );
    }


    /**
     * Get reservations by user.
     *
     * GET /api/reservations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<ReservationDTO>> getReservationsByUser(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                reservationService.getReservationsByUser(userId)
        );
    }


    /**
     * Get reservations by trip.
     *
     * GET /api/reservations/trip/{tripId}
     */
    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<List<ReservationDTO>> getReservationsByTrip(
            @PathVariable Long tripId
    ) {

        return ResponseEntity.ok(
                reservationService.getReservationsByTrip(tripId)
        );
    }


    /**
     * Get current student's reservation for a trip.
     *
     * GET /api/reservations/my/user/{userId}/trip/{tripId}
     */
    @GetMapping("/my/user/{userId}/trip/{tripId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ReservationDTO> getUserTripReservation(
            @PathVariable Long userId,
            @PathVariable Long tripId
    ) {

        return ResponseEntity.ok(
                reservationService.getUserTripReservation(
                        userId,
                        tripId
                )
        );
    }


    /**
     * Cancel reservation.
     *
     * DELETE /api/reservations/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<ReservationDTO> cancelReservation(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                reservationService.cancelReservation(id)
        );
    }


    /**
     * Board passenger using QR.
     *
     * POST /api/reservations/board/{qr}
     */
    @PostMapping("/board/{qr}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<ReservationDTO> boardByQr(
            @PathVariable String qr
    ) {

        return ResponseEntity.ok(
                reservationService.boardByQr(qr)
        );
    }
}