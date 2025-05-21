package com.eventmanagement.repositories;

import com.eventmanagement.entities.EventRegistration;
import com.eventmanagement.entities.User;
import com.eventmanagement.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByUserAndEvent(User user, Event event);
    List<EventRegistration> findByEvent(Event event);
    boolean existsByEventAndUser(Event event, User user);  // Prevents duplicate registrations
    // Fetch attended users for a specific event (but only if the event belongs to the organizer)
    List<EventRegistration> findByEventIdAndEventOrganizerUsernameAndAttendedTrue(Long eventId, String username);

    @Query("SELECT SUM(er.quantity) FROM EventRegistration er WHERE er.event.id = :eventId")
    Integer findTotalBookedTickets(@Param("eventId") Long eventId);

    // Find all event registrations for a specific user
    List<EventRegistration> findByUser(User user);

}
