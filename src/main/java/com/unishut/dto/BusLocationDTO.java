package com.unishut.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Data Transfer Object used for sending a bus GPS location
 * from a driver client or GPS simulator to the uniShut backend.
 *
 * Typical communication flow:
 *
 * Driver/GPS Simulator
 *        |
 *        | TCP Socket
 *        v
 * Socket Server
 *        |
 *        v
 * BusLocationDTO
 *        |
 *        v
 * Tracking Service
 *        |
 *        v
 * Location Entity
 */
public class BusLocationDTO {

    /**
     * ID of the bus sending the location.
     */
    @NotNull(message = "Bus ID is required")
    @Positive(message = "Bus ID must be a positive number")
    private Long busId;


    /**
     * ID of the currently active trip.
     *
     * This can be null when the bus is not operating
     * on an active trip.
     */
    @Positive(message = "Trip ID must be a positive number")
    private Long tripId;


    /**
     * GPS latitude.
     *
     * Valid geographical range:
     * -90 to +90
     */
    @NotNull(message = "Latitude is required")
    @DecimalMin(
            value = "-90.0",
            message = "Latitude must be greater than or equal to -90"
    )
    @DecimalMax(
            value = "90.0",
            message = "Latitude must be less than or equal to 90"
    )
    private Double latitude;


    /**
     * GPS longitude.
     *
     * Valid geographical range:
     * -180 to +180
     */
    @NotNull(message = "Longitude is required")
    @DecimalMin(
            value = "-180.0",
            message = "Longitude must be greater than or equal to -180"
    )
    @DecimalMax(
            value = "180.0",
            message = "Longitude must be less than or equal to 180"
    )
    private Double longitude;


    /**
     * Time at which the GPS reading was generated.
     *
     * If this value is not supplied by the client,
     * the service layer can use the server timestamp.
     */
    private LocalDateTime timestamp;


    /**
     * Optional vehicle speed in kilometres per hour.
     *
     * Used later for ETA calculations.
     */
    @DecimalMin(
            value = "0.0",
            message = "Speed cannot be negative"
    )
    private Double speedKmh;


    /**
     * Source of the GPS update.
     *
     * Examples:
     *
     * TCP_SOCKET
     * GPS_SIMULATOR
     * DRIVER_APP
     */
    @Size(
            max = 30,
            message = "Source must not exceed 30 characters"
    )
    private String source;


    /**
     * Required by Jackson for JSON deserialization.
     */
    public BusLocationDTO() {
    }


    /**
     * Constructor for creating a location DTO.
     */
    public BusLocationDTO(
            Long busId,
            Long tripId,
            Double latitude,
            Double longitude,
            LocalDateTime timestamp,
            Double speedKmh,
            String source
    ) {
        this.busId = busId;
        this.tripId = tripId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.speedKmh = speedKmh;
        this.source = source;
    }


    public Long getBusId() {
        return busId;
    }


    public void setBusId(Long busId) {
        this.busId = busId;
    }


    public Long getTripId() {
        return tripId;
    }


    public void setTripId(Long tripId) {
        this.tripId = tripId;
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


    public LocalDateTime getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


    public Double getSpeedKmh() {
        return speedKmh;
    }


    public void setSpeedKmh(Double speedKmh) {
        this.speedKmh = speedKmh;
    }


    public String getSource() {
        return source;
    }


    public void setSource(String source) {
        this.source = source;
    }
}