package com.unishut.repository;

import com.unishut.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /**
     * Find a route by its exact name.
     *
     * Example:
     * Campus ↔ Dhanmondi
     */
    Optional<Route> findByName(String name);


    /**
     * Check whether a route with the given name already exists.
     */
    boolean existsByName(String name);


    /**
     * Find routes by origin.
     */
    List<Route> findByOriginIgnoreCase(String origin);


    /**
     * Find routes by destination.
     */
    List<Route> findByDestinationIgnoreCase(String destination);


    /**
     * Find routes matching both origin and destination.
     */
    List<Route> findByOriginIgnoreCaseAndDestinationIgnoreCase(
            String origin,
            String destination
    );


    /**
     * Search routes by name.
     *
     * The service layer can pass a value such as:
     * "campus"
     *
     * and Spring Data will search:
     * "%campus%"
     */
    List<Route> findByNameContainingIgnoreCase(String keyword);


    /**
     * Search routes by origin or destination.
     *
     * Useful for the frontend route search box.
     */
    List<Route> findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(
            String originKeyword,
            String destinationKeyword
    );
}