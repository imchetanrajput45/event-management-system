package com.eventmanagement.repositories;

import com.eventmanagement.entities.Event;
import com.eventmanagement.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer_Username(String username); // Find events by organizer
    List<Event> findByOrganizer(User organizer);

    // Search events by name (case-insensitive)
    List<Event> findByNameContainingIgnoreCase(String name);

    //  Search events by category (case-insensitive)
    //List<Event> findByCategoryContainingIgnoreCase(String category);

    List<Event> findByDateAfter(String date);
    List<Event> findByDateBefore(String date);

    List<Event> findByDate(String date); // Fetch events by date


    // Search Query (No Changes Needed)
    //List<Event> findByNameContainingIgnoreCase(String query);

    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Event> findAll(Pageable pageable);
}
