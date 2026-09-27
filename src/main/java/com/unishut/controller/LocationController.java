package com.unishut.controller;

import com.unishut.dto.BusLocationDTO;
import com.unishut.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/locations")
public class LocationController {


    private final LocationService locationService;


    public LocationController(
            LocationService locationService
    ) {
        this.locationService = locationService;
    }



    /**
     * Update bus GPS location
     *
     * POST /api/locations
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER')")
    public ResponseEntity<BusLocationDTO> updateLocation(
            @RequestBody BusLocationDTO locationDTO
    ) {

        return ResponseEntity.ok(
                locationService.processLocation(
                        locationDTO
                )
        );
    }



    /**
     * Get latest bus location
     *
     * GET /api/locations/bus/{busId}
     */
    @GetMapping("/bus/{busId}")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<BusLocationDTO> getLatestBusLocation(
            @PathVariable Long busId
    ) {

        return ResponseEntity.ok(
                locationService.getLatestBusLocation(
                        busId
                )
        );
    }



    /**
     * Get latest trip location
     *
     * GET /api/locations/trip/{tripId}
     */
    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<BusLocationDTO> getLatestTripLocation(
            @PathVariable Long tripId
    ) {

        return ResponseEntity.ok(
                locationService.getLatestTripLocation(
                        tripId
                )
        );
    }



    /**
     * Get latest bus trip location
     *
     * GET /api/locations/bus/{busId}/trip/{tripId}
     */
    @GetMapping("/bus/{busId}/trip/{tripId}")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<BusLocationDTO> getLatestBusTripLocation(
            @PathVariable Long busId,
            @PathVariable Long tripId
    ) {

        return ResponseEntity.ok(
                locationService.getLatestBusTripLocation(
                        busId,
                        tripId
                )
        );
    }



    /**
     * Get location history
     *
     * GET /api/locations/bus/{busId}/history?limit=50
     */
    @GetMapping("/bus/{busId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','DRIVER','STUDENT')")
    public ResponseEntity<List<BusLocationDTO>> getBusLocationHistory(
            @PathVariable Long busId,
            @RequestParam(defaultValue = "50") int limit
    ) {

        return ResponseEntity.ok(
                locationService.getBusLocationHistory(
                        busId,
                        limit
                )
        );
    }

}