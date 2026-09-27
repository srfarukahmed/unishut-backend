package com.unishut.service;

import com.unishut.dto.BusDTO;
import com.unishut.exception.BadRequestException;
import com.unishut.exception.ResourceNotFoundException;
import com.unishut.model.Bus;
import com.unishut.model.User;
import com.unishut.repository.BusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.unishut.service.NotificationService;

/**
 * Service responsible for bus-related business logic.
 *
 * Responsibilities:
 *
 * - Create buses
 * - Update buses
 * - Find buses
 * - Delete buses
 * - Assign drivers
 * - Remove drivers
 * - Validate bus information
 * - Convert Bus entities into BusDTO objects
 */
@Service
@Transactional
public class BusService {

    private final BusRepository busRepository;

    private final UserService userService;
    private final NotificationService notificationService;


    /**
     * Constructor injection.
     */
public BusService(
        BusRepository busRepository,
        UserService userService,
        NotificationService notificationService
) {
    this.busRepository = busRepository;
    this.userService = userService;
    this.notificationService = notificationService;
}


    /**
     * Create a new bus.
     *
     * @param busDTO bus information
     * @return created bus
     */
    public BusDTO createBus(
            BusDTO busDTO
    ) {

        validateBusDTO(busDTO);


        String number = normalize(
                busDTO.getNumber()
        );

        String plate = normalize(
                busDTO.getPlate()
        );


        /*
         * Prevent duplicate bus numbers.
         */
        if (busRepository.existsByNumber(number)) {

            throw new BadRequestException(
                    "Bus number is already registered: "
                            + number
            );
        }


        /*
         * Prevent duplicate vehicle plates.
         */
        if (busRepository.existsByPlate(plate)) {

            throw new BadRequestException(
                    "Bus plate is already registered: "
                            + plate
            );
        }


        Bus bus = new Bus();

        bus.setNumber(number);

        bus.setPlate(plate);

        bus.setCapacity(
                busDTO.getCapacity()
        );

        bus.setStatus(
                normalizeStatus(
                        busDTO.getStatus()
                )
        );


        /*
         * Driver assignment is optional during creation.
         */
        if (busDTO.getDriverId() != null) {

            User driver = getValidDriver(
                    busDTO.getDriverId()
            );

            bus.setDriver(driver);
        }


        Bus savedBus = busRepository.save(bus);

        return toDTO(savedBus);
    }


    /**
     * Find a bus by ID.
     *
     * @param id bus ID
     * @return bus DTO
     */
    @Transactional(readOnly = true)
    public BusDTO getBusById(
            Long id
    ) {

        return toDTO(
                getBusEntityById(id)
        );
    }


    /**
     * Find all buses.
     *
     * @return all buses
     */
    @Transactional(readOnly = true)
    public List<BusDTO> getAllBuses() {

        return busRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Find buses by status.
     *
     * Examples:
     *
     * ACTIVE
     * INACTIVE
     * MAINTENANCE
     *
     * @param status bus status
     * @return matching buses
     */
    @Transactional(readOnly = true)
    public List<BusDTO> getBusesByStatus(
            String status
    ) {

        String normalizedStatus =
                normalizeStatus(status);


        return busRepository
                .findByStatusIgnoreCase(
                        normalizedStatus
                )
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Find a bus by its bus number.
     *
     * @param number bus number
     * @return bus DTO
     */
    @Transactional(readOnly = true)
    public BusDTO getBusByNumber(
            String number
    ) {

        String normalizedNumber =
                normalize(number);


        Bus bus = busRepository
                .findByNumber(normalizedNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bus not found with number: "
                                        + normalizedNumber
                        )
                );


        return toDTO(bus);
    }


    /**
     * Find the bus assigned to a driver.
     *
     * @param driverId driver user ID
     * @return assigned bus
     */
    @Transactional(readOnly = true)
    public BusDTO getBusByDriver(
            Long driverId
    ) {

        User driver =
                userService.getUserEntityById(
                        driverId
                );


        validateDriverRole(driver);


        Bus bus = busRepository
                .findFirstByDriver(driver)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No bus is assigned to driver ID: "
                                        + driverId
                        )
                );


        return toDTO(bus);
    }


    /**
     * Update an existing bus.
     *
     * @param id bus ID
     * @param busDTO new bus information
     * @return updated bus
     */
    public BusDTO updateBus(
            Long id,
            BusDTO busDTO
    ) {

        validateBusDTO(busDTO);


        Bus bus = getBusEntityById(id);


        String number = normalize(
                busDTO.getNumber()
        );

        String plate = normalize(
                busDTO.getPlate()
        );


        /*
         * If the bus number is being changed, make sure
         * another bus does not already use it.
         */
        if (!number.equalsIgnoreCase(
                bus.getNumber()
        )
                && busRepository.existsByNumber(number)) {

            throw new BadRequestException(
                    "Bus number is already registered: "
                            + number
            );
        }


        /*
         * If the plate is being changed, make sure
         * another bus does not already use it.
         */
        if (!plate.equalsIgnoreCase(
                bus.getPlate()
        )
                && busRepository.existsByPlate(plate)) {

            throw new BadRequestException(
                    "Bus plate is already registered: "
                            + plate
            );
        }


        bus.setNumber(number);

        bus.setPlate(plate);

        bus.setCapacity(
                busDTO.getCapacity()
        );

        bus.setStatus(
                normalizeStatus(
                        busDTO.getStatus()
                )
        );


        /*
         * A null driverId means remove the current driver.
         */
        if (busDTO.getDriverId() == null) {

            bus.setDriver(null);

        } else {

            User driver = getValidDriver(
                    busDTO.getDriverId()
            );

            bus.setDriver(driver);
        }


        return toDTO(
                busRepository.save(bus)
        );
    }

    /**
 * Update current GPS location of a bus.
 *
 * @param busId bus ID
 * @param latitude current latitude
 * @param longitude current longitude
 */
public BusDTO updateLocation(
        Long busId,
        Double latitude,
        Double longitude
) {

    Bus bus =
            getBusEntityById(busId);


    bus.setLatitude(latitude);

    bus.setLongitude(longitude);


    Bus savedBus =
            busRepository.save(bus);


    return toDTO(savedBus);
}


    /**
     * Assign a driver to a bus.
     *
     * @param busId bus ID
     * @param driverId driver user ID
     * @return updated bus
     */
    public BusDTO assignDriver(
            Long busId,
            Long driverId
    ) {

        Bus bus =
                getBusEntityById(busId);


        User driver =
                getValidDriver(driverId);


        /*
         * If this driver is already assigned to another bus,
         * prevent the conflicting assignment.
         */
        List<Bus> driverBuses =
                busRepository.findByDriver(driver);


        boolean assignedToAnotherBus =
                driverBuses.stream()
                        .anyMatch(existingBus ->
                                existingBus.getId() != null
                                        && !existingBus.getId()
                                        .equals(busId)
                        );


        if (assignedToAnotherBus) {

            throw new BadRequestException(
                    "Driver is already assigned to another bus"
            );
        }


        bus.setDriver(driver);


        return toDTO(
                busRepository.save(bus)
        );
    }


    /**
     * Remove the currently assigned driver from a bus.
     *
     * @param busId bus ID
     * @return updated bus
     */
    public BusDTO removeDriver(
            Long busId
    ) {

        Bus bus =
                getBusEntityById(busId);


        if (bus.getDriver() == null) {

            throw new BadRequestException(
                    "No driver is currently assigned to this bus"
            );
        }


        bus.setDriver(null);


        return toDTO(
                busRepository.save(bus)
        );
    }


    /**
     * Change the status of a bus.
     *
     * @param busId bus ID
     * @param status new status
     * @return updated bus
     */
public BusDTO updateStatus(
        Long busId,
        String status
) {

    Bus bus =
            getBusEntityById(busId);


    String normalizedStatus =
            normalizeStatus(status);


    bus.setStatus(
            normalizedStatus
    );


    Bus savedBus =
            busRepository.save(bus);


    /*
     * Notify assigned driver if exists
     */
    if (savedBus.getDriver() != null) {

        notificationService.createBusNotification(
                savedBus.getDriver().getId(),
                "Bus "
                        + savedBus.getNumber()
                        + " status changed to "
                        + savedBus.getStatus()
                        + "."
        );
    }


    return toDTO(savedBus);
}

    /**
     * Delete a bus.
     *
     * This operation should normally be restricted to
     * administrators at the controller/security layer.
     *
     * @param id bus ID
     */
    public void deleteBus(
            Long id
    ) {

        Bus bus =
                getBusEntityById(id);


        /*
         * Do not allow deletion while a driver is assigned.
         * The admin should first remove the assignment.
         */
        if (bus.getDriver() != null) {

            throw new BadRequestException(
                    "Cannot delete a bus while a driver is assigned"
            );
        }


        busRepository.delete(bus);
    }


    /**
     * Get the actual Bus entity.
     *
     * Used internally by other services such as TripService.
     *
     * @param id bus ID
     * @return Bus entity
     */
    @Transactional(readOnly = true)
    public Bus getBusEntityById(
            Long id
    ) {

        if (id == null) {

            throw new BadRequestException(
                    "Bus ID is required"
            );
        }


        return busRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bus not found with ID: "
                                        + id
                        )
                );
    }


    /**
     * Validate that a user is actually a driver.
     */
    private User getValidDriver(
            Long driverId
    ) {

        if (driverId == null) {

            throw new BadRequestException(
                    "Driver ID is required"
            );
        }


        User driver =
                userService.getUserEntityById(
                        driverId
                );


        validateDriverRole(driver);


        if (!driver.isEnabled()) {

            throw new BadRequestException(
                    "Driver account is disabled"
            );
        }


        return driver;
    }


    /**
     * Validate the user's role.
     */
    private void validateDriverRole(
            User user
    ) {

        if (user == null
                || user.getRole() == null
                || !"DRIVER".equalsIgnoreCase(
                user.getRole()
        )) {

            throw new BadRequestException(
                    "Selected user is not a driver"
            );
        }
    }


    /**
     * Validate incoming bus information.
     */
    private void validateBusDTO(
            BusDTO busDTO
    ) {

        if (busDTO == null) {

            throw new BadRequestException(
                    "Bus information is required"
            );
        }


        if (busDTO.getNumber() == null
                || busDTO.getNumber().isBlank()) {

            throw new BadRequestException(
                    "Bus number is required"
            );
        }


        if (busDTO.getPlate() == null
                || busDTO.getPlate().isBlank()) {

            throw new BadRequestException(
                    "Bus plate is required"
            );
        }


        if (busDTO.getCapacity() == null
                || busDTO.getCapacity() <= 0) {

            throw new BadRequestException(
                    "Bus capacity must be greater than zero"
            );
        }


        if (busDTO.getCapacity() > 500) {

            throw new BadRequestException(
                    "Bus capacity cannot exceed 500"
            );
        }


        if (busDTO.getStatus() == null
                || busDTO.getStatus().isBlank()) {

            throw new BadRequestException(
                    "Bus status is required"
            );
        }
    }


    /**
     * Normalize bus status.
     */
    private String normalizeStatus(
            String status
    ) {

        if (status == null || status.isBlank()) {

            throw new BadRequestException(
                    "Bus status is required"
            );
        }


        String normalized =
                status.trim()
                        .toUpperCase(Locale.ROOT);


        if (!normalized.equals("ACTIVE")
                && !normalized.equals("INACTIVE")
                && !normalized.equals("MAINTENANCE")) {

            throw new BadRequestException(
                    "Invalid bus status: " + status
            );
        }


        return normalized;
    }


    /**
     * Trim and validate a required string.
     */
    private String normalize(
            String value
    ) {

        if (value == null || value.isBlank()) {

            throw new BadRequestException(
                    "Required value cannot be empty"
            );
        }


        return value.trim();
    }


    /**
     * Convert Bus entity into BusDTO.
     */
    public BusDTO toDTO(
            Bus bus
    ) {

        if (bus == null) {
            return null;
        }


        Long driverId = null;

        String driverName = null;


        if (bus.getDriver() != null) {

            driverId =
                    bus.getDriver().getId();

            driverName =
                    bus.getDriver().getName();
        }

return new BusDTO(
        bus.getId(),
        bus.getNumber(),
        bus.getPlate(),
        bus.getCapacity(),
        bus.getStatus(),
        driverId,
        driverName,
        bus.getLatitude(),
        bus.getLongitude()
);
    }
}