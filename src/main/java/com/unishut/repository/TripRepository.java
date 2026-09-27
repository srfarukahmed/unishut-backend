package com.unishut.repository;

import com.unishut.model.Bus;
import com.unishut.model.Route;
import com.unishut.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository responsible for database operations related to Trip.
 *
 * Spring Data JPA automatically generates the required SQL
 * queries from the method names.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Find trips by status.
     *
     * Example:
     * SCHEDULED
     * BOARDING
     * IN_PROGRESS
     * COMPLETED
     * CANCELLED
     */
    List<Trip> findByStatusIgnoreCase(
            String status
    );


    /**
     * Find trips by status ordered by start time.
     */
    List<Trip> findByStatusIgnoreCaseOrderByStartTimeAsc(
            String status
    );


    /**
     * Find trips that start after a specific date/time.
     *
     * Results are ordered chronologically.
     */
    List<Trip> findByStartTimeAfterOrderByStartTimeAsc(
            LocalDateTime startTime
    );


    /**
     * Find upcoming trips for a specific route.
     *
     * Results are ordered by start time.
     */
    List<Trip> findByRouteAndStartTimeAfterOrderByStartTimeAsc(
            Route route,
            LocalDateTime startTime
    );


    /**
     * Find upcoming trips for a specific bus.
     *
     * Results are ordered by start time.
     */
    List<Trip> findByBusAndStartTimeAfterOrderByStartTimeAsc(
            Bus bus,
            LocalDateTime startTime
    );


    /**
     * Find trips belonging to a particular bus and status.
     *
     * This method is used by GPS tracking to determine
     * which trip is currently associated with a bus.
     */
    List<Trip> findByBusAndStatusIgnoreCase(
            Bus bus,
            String status
    );


    /**
     * Find a trip belonging to a bus at an exact start time.
     */
    Optional<Trip> findByBusAndStartTime(
            Bus bus,
            LocalDateTime startTime
    );


    /**
     * Find all trips belonging to a route.
     */
    List<Trip> findByRoute(
            Route route
    );


    /**
     * Find all trips belonging to a bus.
     */
    List<Trip> findByBus(
            Bus bus
    );
}