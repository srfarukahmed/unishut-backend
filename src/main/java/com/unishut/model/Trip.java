package com.unishut.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents one scheduled or active shuttle trip
 * in the uniShut system.
 *
 * A Trip connects:
 *
 * Route
 *   +
 * Bus
 *   +
 * Start Time
 *   +
 * Live Tracking Information
 *
 * Example:
 *
 * Route       : Campus ↔ Dhanmondi
 * Bus         : BUS-01
 * Start Time  : 08:00
 * Status      : SCHEDULED
 */
@Entity
@Table(
        name = "trips",
        indexes = {
                @Index(
                        name = "idx_trip_route",
                        columnList = "route_id"
                ),
                @Index(
                        name = "idx_trip_bus",
                        columnList = "bus_id"
                ),
                @Index(
                        name = "idx_trip_start_time",
                        columnList = "start_time"
                ),
                @Index(
                        name = "idx_trip_status",
                        columnList = "status"
                )
        }
)
public class Trip {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Route used by this trip.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "route_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_trip_route"
            )
    )
    private Route route;


    /**
     * Bus assigned to this trip.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bus_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_trip_bus"
            )
    )
    private Bus bus;


    /**
     * Scheduled starting time of the trip.
     */
    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalDateTime startTime;


    /**
     * Current trip status.
     *
     * Expected values:
     *
     * SCHEDULED
     * BOARDING
     * IN_PROGRESS
     * COMPLETED
     * CANCELLED
     */
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;


    /**
     * Number of confirmed/reserved passengers.
     */
    @Column(
            name = "reserved",
            nullable = false
    )
    private Integer reserved = 0;


    /**
     * Number of passengers currently waitlisted.
     */
    @Column(
            name = "waitlisted",
            nullable = false
    )
    private Integer waitlisted = 0;


    /**
     * Estimated arrival time in minutes.
     *
     * This value can be updated by the live tracking system.
     */
    @Column(
            name = "eta_min"
    )
    private Integer etaMin;


    /**
     * Current bus latitude.
     *
     * Updated by the real-time tracking system.
     */
    @Column(
            name = "latitude"
    )
    private Double latitude;


    /**
     * Current bus longitude.
     *
     * Updated by the real-time tracking system.
     */
    @Column(
            name = "longitude"
    )
    private Double longitude;


    /**
     * Record creation timestamp.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    /**
     * Last modification timestamp.
     */
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    /**
     * Required by JPA.
     */
    public Trip() {
    }


    /**
     * Constructor for creating a new trip.
     */
    public Trip(
            Route route,
            Bus bus,
            LocalDateTime startTime,
            String status
    ) {
        this.route = route;
        this.bus = bus;
        this.startTime = startTime;
        this.status = status;
        this.reserved = 0;
        this.waitlisted = 0;
    }


    /**
     * Set timestamps before inserting a new trip.
     */
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        normalizeCounters();
    }


    /**
     * Update timestamp before updating a trip.
     */
    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();

        normalizeCounters();
    }


    /**
     * Prevent reservation counters from becoming negative.
     */
    private void normalizeCounters() {

        if (reserved == null || reserved < 0) {
            reserved = 0;
        }

        if (waitlisted == null || waitlisted < 0) {
            waitlisted = 0;
        }
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Route getRoute() {
        return route;
    }


    public void setRoute(Route route) {
        this.route = route;
    }


    public Bus getBus() {
        return bus;
    }


    public void setBus(Bus bus) {
        this.bus = bus;
    }


    public LocalDateTime getStartTime() {
        return startTime;
    }


    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public Integer getReserved() {
        return reserved;
    }


    public void setReserved(Integer reserved) {

        this.reserved = reserved == null || reserved < 0
                ? 0
                : reserved;
    }


    public Integer getWaitlisted() {
        return waitlisted;
    }


    public void setWaitlisted(Integer waitlisted) {

        this.waitlisted = waitlisted == null || waitlisted < 0
                ? 0
                : waitlisted;
    }


    public Integer getEtaMin() {
        return etaMin;
    }


    public void setEtaMin(Integer etaMin) {

        this.etaMin = etaMin;
    }


    public Double getLatitude() {
        return latitude;
    }


    public void setLatitude(Double latitude) {

        this.latitude = latitude;
    }


    public Double getLongitude() {
        return longitude;
    }


    public void setLongitude(Double longitude) {

        this.longitude = longitude;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}