package com.unishut.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a university shuttle bus in the uniShut system.
 *
 * A bus can optionally be assigned to a driver.
 *
 * The driver is represented by the User entity because
 * drivers are also system users with the DRIVER role.
 */
@Entity
@Table(
        name = "buses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_buses_number",
                        columnNames = "number"
                ),
                @UniqueConstraint(
                        name = "uk_buses_plate",
                        columnNames = "plate"
                )
        }
)
public class Bus {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Bus number.
     *
     * Example:
     * BUS-01
     * BUS-02
     */
    @Column(
            name = "number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String number;


    /**
     * Vehicle registration/number plate.
     *
     * Example:
     * DHAKA-METRO-B-11-2044
     */
    @Column(
            name = "plate",
            nullable = false,
            unique = true,
            length = 50
    )
    private String plate;


    /**
     * Maximum passenger capacity of the bus.
     */
    @Column(
            name = "capacity",
            nullable = false
    )
    private Integer capacity;


    /**
     * Current bus status.
     *
     * Possible values used by the application:
     *
     * ACTIVE
     * INACTIVE
     * MAINTENANCE
     */
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private String status;

    /**
 * Current GPS latitude of the bus.
 */
@Column(
        name = "latitude"
)
private Double latitude;


/**
 * Current GPS longitude of the bus.
 */
@Column(
        name = "longitude"
)
private Double longitude;


    /**
     * Driver assigned to this bus.
     *
     * A bus may temporarily have no driver assigned,
     * therefore this relationship is nullable.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "driver_id",
            foreignKey = @ForeignKey(
                    name = "fk_bus_driver"
            )
    )
    private User driver;


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
    public Bus() {
    }


    /**
     * Constructor for creating a new bus.
     */
    public Bus(
            String number,
            String plate,
            Integer capacity,
            String status
    ) {
        this.number = number;
        this.plate = plate;
        this.capacity = capacity;
        this.status = status;
    }


    /**
     * Automatically set timestamps when a bus is created.
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


    public String getNumber() {
        return number;
    }


    public void setNumber(String number) {
        this.number = number;
    }


    public String getPlate() {
        return plate;
    }


    public void setPlate(String plate) {
        this.plate = plate;
    }


    public Integer getCapacity() {
        return capacity;
    }


    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public User getDriver() {
        return driver;
    }


    public void setDriver(User driver) {
        this.driver = driver;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

