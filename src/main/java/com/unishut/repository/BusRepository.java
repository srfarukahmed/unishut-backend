package com.unishut.repository;

import com.unishut.model.Bus;
import com.unishut.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    /**
     * Find a bus by its bus number.
     *
     * Example:
     * BUS-01
     */
    Optional<Bus> findByNumber(String number);


    /**
     * Find a bus by its registration/number plate.
     */
    Optional<Bus> findByPlate(String plate);


    /**
     * Check whether a bus number already exists.
     */
    boolean existsByNumber(String number);


    /**
     * Check whether a vehicle plate already exists.
     */
    boolean existsByPlate(String plate);


    /**
     * Find all buses with a particular status.
     *
     * Examples:
     * ACTIVE
     * INACTIVE
     * MAINTENANCE
     */
    List<Bus> findByStatus(String status);


    /**
     * Find all active buses.
     */
    List<Bus> findByStatusIgnoreCase(String status);


    /**
     * Find all buses assigned to a particular driver.
     */
    List<Bus> findByDriver(User driver);


    /**
     * Find the bus assigned to a particular driver.
     *
     * A driver can normally have one active bus assignment,
     * but Optional is used here so the repository remains
     * safe if no assignment exists.
     */
    Optional<Bus> findFirstByDriver(User driver);


    /**
     * Find all buses that currently have no assigned driver.
     */
    List<Bus> findByDriverIsNull();


    /**
     * Find buses by capacity greater than or equal to
     * the requested capacity.
     */
    List<Bus> findByCapacityGreaterThanEqual(Integer capacity);
}