package com.unishut.controller;

import com.unishut.dto.TripDTO;
import com.unishut.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for UniShut trip management.
 *
 * Base URL:
 *
 * /api/trips
 *
 * Students and drivers can read trip information.
 * Administrators can create, update and delete trips.
 */
@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;


    /**
     * Constructor injection.
     */
    public TripController(
            TripService tripService
    ) {
        this.tripService = tripService;
    }


    /**
     * Get all trips.
     *
     * GET /api/trips
     *
     * Accessible to:
     * STUDENT
     * DRIVER
     * ADMIN
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<TripDTO>> getAllTrips() {

        return ResponseEntity.ok(
                tripService.getAllTrips()
        );
    }


    /**
     * Get a trip by ID.
     *
     * GET /api/trips/{id}
     */
@GetMapping("/{id:\\d+}")
@PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
public ResponseEntity<TripDTO> getTripById(
        @PathVariable Long id
) {

    return ResponseEntity.ok(
            tripService.getTripById(id)
    );
}


    /**
     * Get trips by status.
     *
     * GET /api/trips/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<TripDTO>> getTripsByStatus(
            @PathVariable String status
    ) {

        return ResponseEntity.ok(
                tripService.getTripsByStatus(status)
        );
    }


    /**
     * Get upcoming trips.
     *
     * GET /api/trips/upcoming
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<TripDTO>> getUpcomingTrips() {

        return ResponseEntity.ok(
                tripService.getUpcomingTrips()
        );
    }


    /**
     * Get active trips.
     *
     * GET /api/trips/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<TripDTO>> getActiveTrips() {

        return ResponseEntity.ok(
                tripService.getActiveTrips()
        );
    }


    /**
     * Create a new trip.
     *
     * ADMIN only.
     *
     * POST /api/trips
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripDTO> createTrip(
            @Valid @RequestBody TripDTO tripDTO
    ) {

        TripDTO createdTrip =
                tripService.createTrip(tripDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTrip);
    }


    /**
     * Update an existing trip.
     *
     * ADMIN only.
     *
     * PUT /api/trips/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripDTO> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripDTO tripDTO
    ) {

        return ResponseEntity.ok(
                tripService.updateTrip(
                        id,
                        tripDTO
                )
        );
    }


    /**
     * Update only the status of a trip.
     *
     * ADMIN only.
     *
     * PATCH /api/trips/{id}/status/{status}
     */
    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripDTO> updateTripStatus(
            @PathVariable Long id,
            @PathVariable String status
    ) {

        return ResponseEntity.ok(
                tripService.updateTripStatus(
                        id,
                        status
                )
        );
    }


    /**
     * Delete a trip.
     *
     * ADMIN only.
     *
     * DELETE /api/trips/{id}
     *
     * Trips with active reservations cannot be deleted.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Long id
    ) {

        tripService.deleteTrip(id);

        return ResponseEntity.noContent().build();
    }
}
