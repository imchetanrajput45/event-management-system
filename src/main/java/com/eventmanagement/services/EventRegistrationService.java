package com.eventmanagement.services;

import com.eventmanagement.entities.Event;
import com.eventmanagement.entities.EventRegistration;
import com.eventmanagement.entities.User;
import com.eventmanagement.repositories.EventRegistrationRepository;
import com.eventmanagement.repositories.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EventRegistrationService {

    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;

    public EventRegistrationService(EventRegistrationRepository eventRegistrationRepository, EventRepository eventRepository) {
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.eventRepository = eventRepository;
    }

    //  BOOK TICKET
    @Transactional
    public void bookTicket(User user, Event event, String ticketType, int quantity) {
        if (event.getAvailableTickets() < quantity) {
            throw new RuntimeException("Not enough tickets available.");
        }

        if (eventRegistrationRepository.findByUserAndEvent(user, event).isPresent()) {
            throw new RuntimeException("User already booked tickets for this event.");
        }

        //  Create new booking
        EventRegistration registration = new EventRegistration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setTicketType(ticketType);
        registration.setQuantity(quantity);
        registration.setStatus("CONFIRMED"); // Default status

        //  Save registration & update event ticket count
        eventRegistrationRepository.save(registration);
        event.setAvailableTickets(event.getAvailableTickets() - quantity);
        eventRepository.save(event);
    }

    //  CANCEL TICKET (Refunds available tickets)
    @Transactional
    public void cancelBooking(User user, Event event) {
        Optional<EventRegistration> registration = eventRegistrationRepository.findByUserAndEvent(user, event);
        if (registration.isPresent()) {
            EventRegistration reg = registration.get();
            event.setAvailableTickets(event.getAvailableTickets() + reg.getQuantity()); // Refund tickets
            eventRegistrationRepository.delete(reg);
            eventRepository.save(event);
        } else {
            throw new RuntimeException("No booking found for cancellation.");
        }
    }

    //  CHECK IF USER IS ALREADY REGISTERED
    public boolean isUserRegistered(User user, Event event) {
        return eventRegistrationRepository.findByUserAndEvent(user, event).isPresent();
    }
}



//package com.eventmanagement.services;
//
//import com.eventmanagement.entities.Event;
//import com.eventmanagement.entities.EventRegistration;
//import com.eventmanagement.entities.User;
//import com.eventmanagement.repositories.EventRegistrationRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class EventRegistrationService {
//
//    private final EventRegistrationRepository eventRegistrationRepository;
//
//    public EventRegistrationService(EventRegistrationRepository eventRegistrationRepository) {
//        this.eventRegistrationRepository = eventRegistrationRepository;
//    }
//
//    public void registerUserForEvent(User user, Event event) {
//        if (eventRegistrationRepository.findByUserAndEvent(user, event).isPresent()) {
//            throw new RuntimeException("User already registered for this event.");
//        }
//
//        EventRegistration registration = new EventRegistration();
//        registration.setUser(user);
//        registration.setEvent(event);
//        eventRegistrationRepository.save(registration);
//    }
//
//    //  NEW: Unregister User
//    public void unregisterUserFromEvent(User user, Event event) {
//        Optional<EventRegistration> registration = eventRegistrationRepository.findByUserAndEvent(user, event);
//        registration.ifPresent(eventRegistrationRepository::delete);
//    }
//}
