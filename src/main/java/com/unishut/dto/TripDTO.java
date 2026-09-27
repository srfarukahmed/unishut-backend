package com.unishut.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for uniShut trip information.
 *
 * This class prevents the JPA Trip entity from being exposed
 * directly through REST APIs.
 *
 * It contains the information required by:
 *
 * - Student dashboard
 * - Driver dashboard
 * - Admin dashboard
 * - Trip search
 * - Live tracking
 */
public class TripDTO {

    /**
     * Trip database ID.
     */
    private Long id;


    /**
     * Route ID used when creating/updating a trip.
     */
    @NotNull(message = "Route ID is required")
    @Positive(message = "Route ID must be a positive number")
    private Long routeId;


    /**
     * Bus ID used when creating/updating a trip.
     */
    @NotNull(message = "Bus ID is required")
    @Positive(message = "Bus ID must be a positive number")
    private Long busId;


    /**
     * Route display name.
     *
     * Example:
     * Campus ↔ Dhanmondi
     */
    private String routeName;


    /**
     * Route origin.
     */
    private String origin;


    /**
     * Route destination.
     */
    private String destination;


    /**
     * Bus display number.
     *
     * Example:
     * BUS-01
     */
    private String busNumber;


    /**
     * Scheduled trip start time.
     */
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
    private String status;


    /**
     * Number of confirmed reservations.
     */
    private Integer reserved;


    /**
     * Number of students currently waitlisted.
     */
    private Integer waitlisted;


    /**
     * Estimated arrival time in minutes.
     */
    private Integer etaMin;


    /**
     * Current bus latitude.
     */
    private Double latitude;


    /**
     * Current bus longitude.
     */
    private Double longitude;


    /**
     * Required by Jackson.
     */
    public TripDTO() {
    }


    /**
     * Constructor for creating a trip request.
     *
     * This constructor is useful when an admin creates
     * a new scheduled trip.
     */
    public TripDTO(
            Long routeId,
            Long busId,
            LocalDateTime startTime,
            String status
    ) {
        this.routeId = routeId;
        this.busId = busId;
        this.startTime = startTime;
        this.status = status;
    }


    /**
     * Complete constructor for API responses.
     */
    public TripDTO(
            Long id,
            Long routeId,
            Long busId,
            String routeName,
            String origin,
            String destination,
            String busNumber,
            LocalDateTime startTime,
            String status,
            Integer reserved,
            Integer waitlisted,
            Integer etaMin,
            Double latitude,
            Double longitude
    ) {
        this.id = id;
        this.routeId = routeId;
        this.busId = busId;
        this.routeName = routeName;
        this.origin = origin;
        this.destination = destination;
        this.busNumber = busNumber;
        this.startTime = startTime;
        this.status = status;
        this.reserved = reserved;
        this.waitlisted = waitlisted;
        this.etaMin = etaMin;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getRouteId() {
        return routeId;
    }


    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }


    public Long getBusId() {
        return busId;
    }


    public void setBusId(Long busId) {
        this.busId = busId;
    }


    public String getRouteName() {
        return routeName;
    }


    public void setRouteName(String routeName) {
        this.routeName = routeName;
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


    public String getBusNumber() {
        return busNumber;
    }


    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
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
        this.reserved = reserved;
    }


    public Integer getWaitlisted() {
        return waitlisted;
    }


    public void setWaitlisted(Integer waitlisted) {
        this.waitlisted = waitlisted;
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
}