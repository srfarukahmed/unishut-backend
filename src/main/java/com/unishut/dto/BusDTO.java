package com.unishut.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for uniShut bus information.
 *
 * This DTO is used when sending bus information between
 * the backend and the frontend.
 *
 * It intentionally does not expose the complete User entity
 * of the assigned driver.
 */
public class BusDTO {

    /**
     * Bus database ID.
     */
    private Long id;


    /**
     * Bus display number.
     *
     * Example:
     * BUS-01
     */
    @NotBlank(message = "Bus number is required")
    @Size(
            max = 30,
            message = "Bus number must not exceed 30 characters"
    )
    private String number;


    /**
     * Vehicle registration/number plate.
     */
    @NotBlank(message = "Bus plate is required")
    @Size(
            max = 50,
            message = "Bus plate must not exceed 50 characters"
    )
    private String plate;


    /**
     * Maximum passenger capacity.
     */
    @NotNull(message = "Bus capacity is required")
    @Min(
            value = 1,
            message = "Bus capacity must be at least 1"
    )
    @Max(
            value = 500,
            message = "Bus capacity cannot exceed 500"
    )
    private Integer capacity;


    /**
     * Current bus status.
     *
     * Expected values:
     *
     * ACTIVE
     * INACTIVE
     * MAINTENANCE
     */
    @NotBlank(message = "Bus status is required")
    @Size(
            max = 20,
            message = "Bus status must not exceed 20 characters"
    )
    private String status;

    /**
 * Current bus latitude.
 */
private Double latitude;


/**
 * Current bus longitude.
 */
private Double longitude;


    /**
     * ID of the assigned driver.
     *
     * Null when no driver is currently assigned.
     */
    @Positive(
            message = "Driver ID must be a positive number"
    )
    private Long driverId;


    /**
     * Name of the assigned driver.
     *
     * This is response information and is not required
     * when creating a new bus.
     */
    private String driverName;


    /**
     * Required by Jackson.
     */
    public BusDTO() {
    }


    /**
     * Constructor for creating/updating a bus.
     */
    public BusDTO(
            String number,
            String plate,
            Integer capacity,
            String status,
            Long driverId
    ) {
        this.number = number;
        this.plate = plate;
        this.capacity = capacity;
        this.status = status;
        this.driverId = driverId;
    }


    /**
     * Complete constructor for API responses.
     */
    public BusDTO(
            Long id,
            String number,
            String plate,
            Integer capacity,
            String status,
            Long driverId,
            String driverName,
            Double latitude,
            Double longitude
    ) {
        this.id = id;
        this.number = number;
        this.plate = plate;
        this.capacity = capacity;
        this.status = status;
        this.driverId = driverId;
        this.driverName = driverName;
        this.latitude = latitude;
        this.longitude = longitude;
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


    public Long getDriverId() {
        return driverId;
    }


    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }


    public String getDriverName() {
        return driverName;
    }


    public void setDriverName(String driverName) {
        this.driverName = driverName;
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