package com.unishut.controller;

import com.unishut.dto.RouteDTO;
import com.unishut.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for UniShut route management.
 *
 * Base URL:
 *
 * /api/routes
 *
 * Students and drivers can read route information.
 * Only administrators can create, update and delete routes.
 */
@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;


    /**
     * Constructor injection.
     */
    public RouteController(
            RouteService routeService
    ) {
        this.routeService = routeService;
    }


    /**
     * Get all routes.
     *
     * GET /api/routes
     *
     * Accessible to:
     * STUDENT
     * DRIVER
     * ADMIN
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<RouteDTO>> getAllRoutes() {

        return ResponseEntity.ok(
                routeService.getAllRoutes()
        );
    }


    /**
     * Get a route by ID.
     *
     * GET /api/routes/{id}
     *
     * Accessible to:
     * STUDENT
     * DRIVER
     * ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<RouteDTO> getRouteById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                routeService.getRouteById(id)
        );
    }


    /**
     * Search routes by origin/destination keyword.
     *
     * GET /api/routes/search?keyword=Campus
     *
     * If keyword is empty, RouteService returns all routes.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<RouteDTO>> searchRoutes(
            @RequestParam(required = false) String keyword
    ) {

        return ResponseEntity.ok(
                routeService.searchRoutes(keyword)
        );
    }


    /**
     * Get routes by origin.
     *
     * GET /api/routes/origin/{origin}
     */
    @GetMapping("/origin/{origin}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<RouteDTO>> getRoutesByOrigin(
            @PathVariable String origin
    ) {

        return ResponseEntity.ok(
                routeService.getRoutesByOrigin(origin)
        );
    }


    /**
     * Get routes by destination.
     *
     * GET /api/routes/destination/{destination}
     */
    @GetMapping("/destination/{destination}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<RouteDTO>> getRoutesByDestination(
            @PathVariable String destination
    ) {

        return ResponseEntity.ok(
                routeService.getRoutesByDestination(destination)
        );
    }


    /**
     * Find routes matching both origin and destination.
     *
     * GET /api/routes/find?origin=Campus&destination=Dhanmondi
     */
    @GetMapping("/find")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'STUDENT')")
    public ResponseEntity<List<RouteDTO>> findRoutes(
            @RequestParam String origin,
            @RequestParam String destination
    ) {

        return ResponseEntity.ok(
                routeService.findRoutes(
                        origin,
                        destination
                )
        );
    }


    /**
     * Create a new route.
     *
     * ADMIN only.
     *
     * POST /api/routes
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteDTO> createRoute(
            @Valid @RequestBody RouteDTO routeDTO
    ) {

        RouteDTO createdRoute =
                routeService.createRoute(routeDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdRoute);
    }


    /**
     * Update an existing route.
     *
     * ADMIN only.
     *
     * PUT /api/routes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteDTO> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteDTO routeDTO
    ) {

        return ResponseEntity.ok(
                routeService.updateRoute(
                        id,
                        routeDTO
                )
        );
    }


    /**
     * Delete a route.
     *
     * ADMIN only.
     *
     * DELETE /api/routes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Long id
    ) {

        routeService.deleteRoute(id);

        return ResponseEntity.noContent().build();
    }
}