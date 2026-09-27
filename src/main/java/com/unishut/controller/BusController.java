package com.unishut.controller;

import com.unishut.dto.BusDTO;
import com.unishut.service.BusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for bus management.
 *
 * Base URL:
 *
 * /api/buses
 */
@RestController
@RequestMapping("/buses")
public class BusController {

        private final BusService busService;

        public BusController(
                        BusService busService) {
                this.busService = busService;
        }

        /**
         * Get all buses.
         *
         * Accessible to every authenticated user.
         *
         * GET /api/buses
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
        public ResponseEntity<List<BusDTO>> getAllBuses() {

                return ResponseEntity.ok(
                                busService.getAllBuses());
        }

        /**
         * Get a bus by ID.
         *
         * GET /api/buses/{id}
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
        public ResponseEntity<BusDTO> getBusById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                busService.getBusById(id));
        }

        /**
         * Get a bus by bus number.
         *
         * GET /api/buses/number/{number}
         */
        @GetMapping("/number/{number}")
        @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
        public ResponseEntity<BusDTO> getBusByNumber(
                        @PathVariable String number) {

                return ResponseEntity.ok(
                                busService.getBusByNumber(number));
        }

        /**
         * Get buses by status.
         *
         * GET /api/buses/status/{status}
         */
        @GetMapping("/status/{status}")
        @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
        public ResponseEntity<List<BusDTO>> getBusesByStatus(
                        @PathVariable String status) {

                return ResponseEntity.ok(
                                busService.getBusesByStatus(status));
        }

        /**
         * Get the bus assigned to a driver.
         *
         * GET /api/buses/driver/{driverId}
         *
         * ADMIN can inspect any driver.
         * DRIVER can use this endpoint for driver-specific data.
         */
        @GetMapping("/driver/{driverId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
        public ResponseEntity<BusDTO> getBusByDriver(
                        @PathVariable Long driverId) {

                return ResponseEntity.ok(
                                busService.getBusByDriver(driverId));
        }

        /**
         * Create a new bus.
         *
         * ADMIN only.
         *
         * POST /api/buses
         */
        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<BusDTO> createBus(
                        @Valid @RequestBody BusDTO busDTO) {

                BusDTO createdBus = busService.createBus(busDTO);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(createdBus);
        }

        /**
         * Update an existing bus.
         *
         * ADMIN only.
         *
         * PUT /api/buses/{id}
         */
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<BusDTO> updateBus(
                        @PathVariable Long id,
                        @Valid @RequestBody BusDTO busDTO) {

                return ResponseEntity.ok(
                                busService.updateBus(
                                                id,
                                                busDTO));
        }

        /**
         * Assign a driver to a bus.
         *
         * ADMIN only.
         *
         * PUT /api/buses/{busId}/driver/{driverId}
         */
        @PutMapping("/{busId}/driver/{driverId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<BusDTO> assignDriver(
                        @PathVariable Long busId,
                        @PathVariable Long driverId) {

                return ResponseEntity.ok(
                                busService.assignDriver(
                                                busId,
                                                driverId));
        }

        /**
         * Remove the driver from a bus.
         *
         * ADMIN only.
         *
         * DELETE /api/buses/{busId}/driver
         */
        @DeleteMapping("/{busId}/driver")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<BusDTO> removeDriver(
                        @PathVariable Long busId) {

                return ResponseEntity.ok(
                                busService.removeDriver(busId));
        }

        /**
         * Update the status of a bus.
         *
         * ADMIN only.
         *
         * PATCH /api/buses/{busId}/status/{status}
         */
        @PatchMapping("/{busId}/status/{status}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<BusDTO> updateStatus(
                        @PathVariable Long busId,
                        @PathVariable String status) {

                return ResponseEntity.ok(
                                busService.updateStatus(
                                                busId,
                                                status));
        }

        /**
         * Update bus GPS location.
         *
         * PATCH /api/buses/{busId}/location
         */
        @PatchMapping("/{busId}/location")
        @PreAuthorize("hasAnyRole('ADMIN','DRIVER')")
        public ResponseEntity<BusDTO> updateLocation(
                        @PathVariable Long busId,
                        @RequestParam Double latitude,
                        @RequestParam Double longitude) {

                return ResponseEntity.ok(
                                busService.updateLocation(
                                                busId,
                                                latitude,
                                                longitude));
        }

        /**
         * Delete a bus.
         *
         * ADMIN only.
         *
         * DELETE /api/buses/{id}
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deleteBus(
                        @PathVariable Long id) {

                busService.deleteBus(id);

                return ResponseEntity.noContent().build();
        }
}