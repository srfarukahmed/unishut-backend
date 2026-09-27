package com.unishut.service;

import com.unishut.dto.RouteDTO;
import com.unishut.exception.BadRequestException;
import com.unishut.exception.ResourceNotFoundException;
import com.unishut.model.Route;
import com.unishut.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for route-related business logic.
 *
 * Responsibilities:
 *
 * - Create routes
 * - Read routes
 * - Update routes
 * - Delete routes
 * - Search routes
 * - Validate route information
 * - Convert Route entities into RouteDTO objects
 */
@Service
@Transactional
public class RouteService {

    private final RouteRepository routeRepository;


    /**
     * Constructor injection.
     */
    public RouteService(
            RouteRepository routeRepository
    ) {
        this.routeRepository = routeRepository;
    }


    /**
     * Create a new route.
     *
     * @param routeDTO route information
     * @return created route
     */
    public RouteDTO createRoute(
            RouteDTO routeDTO
    ) {

        validateRouteDTO(routeDTO);


        String name = normalize(
                routeDTO.getName()
        );

        String origin = normalize(
                routeDTO.getOrigin()
        );

        String destination = normalize(
                routeDTO.getDestination()
        );


        /*
         * A route cannot start and end at the same location.
         */
        if (origin.equalsIgnoreCase(destination)) {

            throw new BadRequestException(
                    "Route origin and destination cannot be the same"
            );
        }


        /*
         * Prevent duplicate route names.
         */
        if (routeRepository.existsByName(name)) {

            throw new BadRequestException(
                    "Route name is already registered: "
                            + name
            );
        }


        Route route = new Route();

        route.setName(name);

        route.setOrigin(origin);

        route.setDestination(destination);

        route.setStops(
                copyStops(routeDTO.getStops())
        );

        route.setDistanceKm(
                routeDTO.getDistanceKm()
        );


        Route savedRoute =
                routeRepository.save(route);


        return toDTO(savedRoute);
    }


    /**
     * Get a route by ID.
     *
     * @param id route ID
     * @return route DTO
     */
    @Transactional(readOnly = true)
    public RouteDTO getRouteById(
            Long id
    ) {

        return toDTO(
                getRouteEntityById(id)
        );
    }


    /**
     * Get all routes.
     *
     * @return all routes
     */
    @Transactional(readOnly = true)
    public List<RouteDTO> getAllRoutes() {

        return routeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Search routes by keyword.
     *
     * The keyword can match either the route origin
     * or destination.
     *
     * @param keyword search keyword
     * @return matching routes
     */
    @Transactional(readOnly = true)
    public List<RouteDTO> searchRoutes(
            String keyword
    ) {

        if (keyword == null
                || keyword.isBlank()) {

            return getAllRoutes();
        }


        String normalizedKeyword =
                keyword.trim();


        return routeRepository
                .findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(
                        normalizedKeyword,
                        normalizedKeyword
                )
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Find routes by origin.
     *
     * @param origin starting location
     * @return matching routes
     */
    @Transactional(readOnly = true)
    public List<RouteDTO> getRoutesByOrigin(
            String origin
    ) {

        if (origin == null || origin.isBlank()) {

            throw new BadRequestException(
                    "Origin is required"
            );
        }


        return routeRepository
                .findByOriginIgnoreCase(
                        origin.trim()
                )
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Find routes by destination.
     *
     * @param destination destination location
     * @return matching routes
     */
    @Transactional(readOnly = true)
    public List<RouteDTO> getRoutesByDestination(
            String destination
    ) {

        if (destination == null
                || destination.isBlank()) {

            throw new BadRequestException(
                    "Destination is required"
            );
        }


        return routeRepository
                .findByDestinationIgnoreCase(
                        destination.trim()
                )
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Find routes matching both origin and destination.
     *
     * @param origin route origin
     * @param destination route destination
     * @return matching routes
     */
    @Transactional(readOnly = true)
    public List<RouteDTO> findRoutes(
            String origin,
            String destination
    ) {

        if (origin == null || origin.isBlank()) {

            throw new BadRequestException(
                    "Origin is required"
            );
        }


        if (destination == null
                || destination.isBlank()) {

            throw new BadRequestException(
                    "Destination is required"
            );
        }


        return routeRepository
                .findByOriginIgnoreCaseAndDestinationIgnoreCase(
                        origin.trim(),
                        destination.trim()
                )
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Update an existing route.
     *
     * @param id route ID
     * @param routeDTO new route information
     * @return updated route
     */
    public RouteDTO updateRoute(
            Long id,
            RouteDTO routeDTO
    ) {

        validateRouteDTO(routeDTO);


        Route route =
                getRouteEntityById(id);


        String name = normalize(
                routeDTO.getName()
        );

        String origin = normalize(
                routeDTO.getOrigin()
        );

        String destination = normalize(
                routeDTO.getDestination()
        );


        if (origin.equalsIgnoreCase(destination)) {

            throw new BadRequestException(
                    "Route origin and destination cannot be the same"
            );
        }


        /*
         * Only check duplicate name when the name
         * is actually being changed.
         */
        if (!name.equalsIgnoreCase(
                route.getName()
        )
                && routeRepository.existsByName(name)) {

            throw new BadRequestException(
                    "Route name is already registered: "
                            + name
            );
        }


        route.setName(name);

        route.setOrigin(origin);

        route.setDestination(destination);

        route.setStops(
                copyStops(routeDTO.getStops())
        );

        route.setDistanceKm(
                routeDTO.getDistanceKm()
        );


        return toDTO(
                routeRepository.save(route)
        );
    }


    /**
     * Delete a route.
     *
     * Trip-related restrictions should also be checked
     * by the TripService/controller before deletion if
     * the route is currently in use.
     *
     * @param id route ID
     */
    public void deleteRoute(
            Long id
    ) {

        Route route =
                getRouteEntityById(id);


        routeRepository.delete(route);
    }


    /**
     * Retrieve the actual Route entity.
     *
     * Other services such as TripService can use this
     * when creating relationships.
     *
     * @param id route ID
     * @return Route entity
     */
    @Transactional(readOnly = true)
    public Route getRouteEntityById(
            Long id
    ) {

        if (id == null) {

            throw new BadRequestException(
                    "Route ID is required"
            );
        }


        return routeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with ID: "
                                        + id
                        )
                );
    }


    /**
     * Convert Route entity to RouteDTO.
     *
     * A defensive copy of the stops list is returned so
     * that the DTO does not directly expose the entity's
     * mutable collection.
     *
     * @param route route entity
     * @return route DTO
     */
    public RouteDTO toDTO(
            Route route
    ) {

        if (route == null) {
            return null;
        }


        List<String> stops =
                route.getStops() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                                route.getStops()
                        );


        return new RouteDTO(
                route.getId(),
                route.getName(),
                route.getOrigin(),
                route.getDestination(),
                stops,
                route.getDistanceKm()
        );
    }


    /**
     * Validate incoming route data.
     */
    private void validateRouteDTO(
            RouteDTO routeDTO
    ) {

        if (routeDTO == null) {

            throw new BadRequestException(
                    "Route information is required"
            );
        }


        if (routeDTO.getName() == null
                || routeDTO.getName().isBlank()) {

            throw new BadRequestException(
                    "Route name is required"
            );
        }


        if (routeDTO.getOrigin() == null
                || routeDTO.getOrigin().isBlank()) {

            throw new BadRequestException(
                    "Route origin is required"
            );
        }


        if (routeDTO.getDestination() == null
                || routeDTO.getDestination().isBlank()) {

            throw new BadRequestException(
                    "Route destination is required"
            );
        }


        if (routeDTO.getStops() == null
                || routeDTO.getStops().isEmpty()) {

            throw new BadRequestException(
                    "At least one route stop is required"
            );
        }


        if (routeDTO.getDistanceKm() == null) {

            throw new BadRequestException(
                    "Route distance is required"
            );
        }


        if (routeDTO.getDistanceKm() < 0) {

            throw new BadRequestException(
                    "Route distance cannot be negative"
            );
        }


        validateStops(
                routeDTO.getStops()
        );
    }


    /**
     * Validate the ordered stop list.
     */
    private void validateStops(
            List<String> stops
    ) {

        for (String stop : stops) {

            if (stop == null
                    || stop.isBlank()) {

                throw new BadRequestException(
                        "Route stops cannot contain blank values"
                );
            }


            if (stop.trim().length() > 150) {

                throw new BadRequestException(
                        "A route stop must not exceed 150 characters"
                );
            }
        }
    }


    /**
     * Make a defensive copy of route stops.
     */
    private List<String> copyStops(
            List<String> stops
    ) {

        if (stops == null) {

            return new ArrayList<>();
        }


        return stops.stream()
                .map(String::trim)
                .collect(Collectors.toCollection(
                        ArrayList::new
                ));
    }


    /**
     * Normalize a required string.
     */
    private String normalize(
            String value
    ) {

        if (value == null || value.isBlank()) {

            throw new BadRequestException(
                    "Required route value cannot be empty"
            );
        }


        return value.trim();
    }
}