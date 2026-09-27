package com.unishut.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Stores a GPS location update received from a uniShut bus.
 *
 * A location record belongs to a bus and may optionally be
 * associated with a currently running trip.
 *
 * Location data can arrive through:
 *
 * Driver Client
 *      ↓
 * TCP Socket
 *      ↓
 * Socket Client Handler
 *      ↓
 * Multithreaded Location Processing
 *      ↓
 * Location Entity
 *      ↓
 * Database
 */
@Entity
@Table(
        name = "locations",
        indexes = {
                @Index(
                        name = "idx_location_bus",
                        columnList = "bus_id"
                ),
                @Index(
                        name = "idx_location_trip",
                        columnList = "trip_id"
                ),
                @Index(
                        name = "idx_location_timestamp",
                        columnList = "recorded_at"
                )
        }
)
public class Location {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Bus whose location is being recorded.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bus_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_location_bus"
            )
    )
    private Bus bus;


    /**
     * Trip associated with this location update.
     *
     * Nullable because a bus can potentially send a
     * location update when it is not currently assigned
     * to an active trip.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trip_id",
            foreignKey = @ForeignKey(
                    name = "fk_location_trip"
            )
    )
    private Trip trip;


    /**
     * GPS latitude.
     *
     * Valid range:
     * -90 to +90
     */
    @Column(
            name = "latitude",
            nullable = false
    )
    private Double latitude;


    /**
     * GPS longitude.
     *
     * Valid range:
     * -180 to +180
     */
    @Column(
            name = "longitude",
            nullable = false
    )
    private Double longitude;


    /**
     * Time at which the GPS reading was recorded.
     */
    @Column(
            name = "recorded_at",
            nullable = false
    )
    private LocalDateTime recordedAt;


    /**
     * Source of the location update.
     *
     * Expected values include:
     *
     * TCP_SOCKET
     * GPS_SIMULATOR
     * DRIVER_APP
     * SYSTEM
     */
    @Column(
            name = "source",
            nullable = false,
            length = 30
    )
    private String source;


    /**
     * Optional speed in kilometres per hour.
     *
     * This is useful for future ETA calculation.
     */
    @Column(
            name = "speed_kmh"
    )
    private Double speedKmh;


    /**
     * Required by JPA.
     */
    public Location() {
    }


    /**
     * Constructor for creating a location record.
     */
    public Location(
            Bus bus,
            Trip trip,
            Double latitude,
            Double longitude,
            LocalDateTime recordedAt,
            String source
    ) {
        this.bus = bus;
        this.trip = trip;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = recordedAt;
        this.source = source;
    }


    /**
     * Automatically assign the current server time if
     * the client does not provide a timestamp.
     */
    @PrePersist
    protected void onCreate() {

        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }

        if (source == null || source.isBlank()) {
            source = "SYSTEM";
        }
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Bus getBus() {
        return bus;
    }


    public void setBus(Bus bus) {
        this.bus = bus;
    }


    public Trip getTrip() {
        return trip;
    }


    public void setTrip(Trip trip) {
        this.trip = trip;
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


    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }


    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }


    public String getSource() {
        return source;
    }


    public void setSource(String source) {
        this.source = source;
    }


    public Double getSpeedKmh() {
        return speedKmh;
    }


    public void setSpeedKmh(Double speedKmh) {
        this.speedKmh = speedKmh;
    }
}