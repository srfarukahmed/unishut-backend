package com.unishut.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shuttle route in the uniShut system.
 *
 * Example:
 *
 * Name        : Campus ↔ Dhanmondi
 * Origin      : Main Campus
 * Destination : Dhanmondi 27
 * Stops       : Main Gate, Science Lab, Kalabagan, Dhanmondi 27
 * Distance    : 10.8 km
 */
@Entity
@Table(
        name = "routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_routes_name",
                        columnNames = "name"
                )
        }
)
public class Route {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Display name of the route.
     *
     * Example:
     * Campus ↔ Dhanmondi
     */
    @Column(
            name = "name",
            nullable = false,
            unique = true,
            length = 150
    )
    private String name;


    /**
     * Starting point of the route.
     */
    @Column(
            name = "origin",
            nullable = false,
            length = 150
    )
    private String origin;


    /**
     * Ending point of the route.
     */
    @Column(
            name = "destination",
            nullable = false,
            length = 150
    )
    private String destination;


    /**
     * Ordered list of route stops.
     *
     * Example:
     *
     * Main Gate
     * Science Lab
     * Kalabagan
     * Dhanmondi 27
     *
     * A separate route_stops table is used rather than
     * storing the stops as a comma-separated string.
     */
    @ElementCollection
    @CollectionTable(
            name = "route_stops",
            joinColumns = @JoinColumn(
                    name = "route_id",
                    foreignKey = @ForeignKey(
                            name = "fk_route_stops_route"
                    )
            )
    )
    @OrderColumn(name = "stop_order")
    @Column(
            name = "stop_name",
            nullable = false,
            length = 150
    )
    private List<String> stops = new ArrayList<>();


    /**
     * Approximate route distance in kilometres.
     */
    @Column(
            name = "distance_km",
            nullable = false
    )
    private Double distanceKm = 0.0;


    /**
     * Record creation time.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    /**
     * Last modification time.
     */
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    /**
     * Required by JPA.
     */
    public Route() {
    }


    /**
     * Constructor for creating a route.
     */
    public Route(
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

        if (distanceKm != null) {
            this.distanceKm = distanceKm;
        }
    }


    /**
     * Automatically set timestamps before inserting
     * a new route.
     */
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }


    /**
     * Automatically update the modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
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


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}