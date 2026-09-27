package com.unishut.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for uniShut route information.
 *
 * Used by:
 *
 * - Student route search
 * - Student route details
 * - Admin route management
 * - Trip creation
 * - Route update
 */
public class RouteDTO {

    /**
     * Route database ID.
     */
    private Long id;


    /**
     * Display name of the route.
     *
     * Example:
     * Campus ↔ Dhanmondi
     */
    @NotBlank(message = "Route name is required")
    @Size(
            max = 150,
            message = "Route name must not exceed 150 characters"
    )
    private String name;


    /**
     * Starting point of the route.
     */
    @NotBlank(message = "Origin is required")
    @Size(
            max = 150,
            message = "Origin must not exceed 150 characters"
    )
    private String origin;


    /**
     * Ending point of the route.
     */
    @NotBlank(message = "Destination is required")
    @Size(
            max = 150,
            message = "Destination must not exceed 150 characters"
    )
    private String destination;


    /**
     * Ordered list of stops on the route.
     */
    @NotEmpty(message = "At least one route stop is required")
    private List<
            @NotBlank(
                    message = "Route stop cannot be blank"
            )
            @Size(
                    max = 150,
                    message = "Route stop must not exceed 150 characters"
            )
            String
            > stops = new ArrayList<>();


    /**
     * Route distance in kilometres.
     */
    @NotNull(message = "Distance is required")
    @PositiveOrZero(message = "Distance cannot be negative")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Distance cannot be negative"
    )
    private Double distanceKm;


    /**
     * Required by Jackson.
     */
    public RouteDTO() {
    }


    /**
     * Constructor for creating a new route.
     */
    public RouteDTO(
            String name,
            String origin,
            String destination,
            List<String> stops,
            Double distanceKm
    ) {
        this.name = name;
        this.origin = origin;
        this.destination = destination;

        if (stops != null) {
            this.stops = new ArrayList<>(stops);
        }

        this.distanceKm = distanceKm;
    }


    /**
     * Complete constructor for API responses.
     */
    public RouteDTO(
            Long id,
            String name,
            String origin,
            String destination,
            List<String> stops,
            Double distanceKm
    ) {
        this.id = id;
        this.name = name;
        this.origin = origin;
        this.destination = destination;

        if (stops != null) {
            this.stops = new ArrayList<>(stops);
        }

        this.distanceKm = distanceKm;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getOrigin() {
        return origin;
    }


    public void setOrigin(String origin) {
        this.origin = origin;
    }


    public String getDestination() {
        return destination;
    }


    public void setDestination(String destination) {
        this.destination = destination;
    }


    public List<String> getStops() {
        return stops;
    }


    public void setStops(List<String> stops) {

        if (stops == null) {
            this.stops = new ArrayList<>();
        } else {
            this.stops = new ArrayList<>(stops);
        }
    }


    public Double getDistanceKm() {
        return distanceKm;
    }


    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}