package com.eventmanagement.services;

import com.eventmanagement.entities.Event;
import com.eventmanagement.entities.EventRegistration;
import com.eventmanagement.entities.Role;
import com.eventmanagement.entities.User;
import com.eventmanagement.repositories.EventRegistrationRepository;
import com.eventmanagement.repositories.EventRepository;
import com.eventmanagement.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    public AttendanceService(EventRepository eventRepository, UserRepository userRepository, EventRegistrationRepository eventRegistrationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    // ✅ Fetch event details with registrations
    public Optional<Event> getEventWithRegistrations(Long eventId) {
        return eventRepository.findById(eventId);
    }

    // ✅ Mark attendance for an event
    public boolean markAttendance(Long eventId, List<Long> attendedUsers, UserDetails userDetails) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            User loggedInUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            // ✅ Check if the logged-in user is the event organizer or an admin
            if (loggedInUser.getRole().equals(Role.ADMIN) || (event.getOrganizer() != null && event.getOrganizer().getUsername().equals(userDetails.getUsername()))) {
                List<EventRegistration> registrations = event.getRegistrations();

                for (EventRegistration registration : registrations) {
                    registration.setAttended(attendedUsers != null && attendedUsers.contains(registration.getUser().getId()));
                    eventRegistrationRepository.save(registration);
                }
                return true; // ✅ Attendance updated successfully
            }
        }
        return false; // ❌ Event not found or unauthorized
    }
}
