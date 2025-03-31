package com.eventmanagement.controllers;

import com.eventmanagement.entities.Event;
import com.eventmanagement.entities.EventRegistration;
import com.eventmanagement.entities.User;
import com.eventmanagement.repositories.EventRegistrationRepository;
import com.eventmanagement.services.EventRegistrationService;
import com.eventmanagement.services.EmailService;
import com.eventmanagement.repositories.EventRepository;
import com.eventmanagement.repositories.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/events")
public class EventRegistrationController {

    private final EventRegistrationService registrationService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public EventRegistrationController(EventRegistrationService registrationService,
                                       EventRepository eventRepository,
                                       UserRepository userRepository,
                                       EmailService emailService) {
        this.registrationService = registrationService;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }


    @GetMapping("/book-page/{eventId}")
    public String showBookingPage(@PathVariable Long eventId, Model model) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            return "redirect:/events"; // Redirect if event not found
        }

        model.addAttribute("event", eventOpt.get());
        return "book-ticket"; // This is the JSP or Thymeleaf page for booking
    }


    // ✅ BOOK TICKET (Updated Registration)
    @PostMapping("/book/{eventId}")
    public String bookTicket(@PathVariable Long eventId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String ticketType,
                             @RequestParam int quantity,
                             RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to book tickets.");
            return "redirect:/login";
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Event not found.");
            return "redirect:/events";
        }

        Event event = eventOpt.get();
        User user = userOpt.get();

        if (event.getAvailableTickets() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Not enough tickets available.");
            return "redirect:/events/" + eventId;
        }

        try {
            registrationService.bookTicket(user, event, ticketType, quantity);
            sendBookingEmail(user, event, ticketType, quantity);
            redirectAttributes.addFlashAttribute("success", "Ticket booked successfully! Check your email.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/events/" + eventId;
    }

    // ✅ CANCEL TICKET (Refunds available tickets)
    @GetMapping("/cancel/{eventId}")
    public String cancelBooking(@PathVariable Long eventId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to cancel booking.");
            return "redirect:/login";
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Event not found.");
            return "redirect:/events";
        }

        Event event = eventOpt.get();
        User user = userOpt.get();

        try {
            registrationService.cancelBooking(user, event);
            sendCancellationEmail(user, event);
            redirectAttributes.addFlashAttribute("success", "Your booking has been canceled.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/events/" + eventId;
    }

    // ✅ Send booking confirmation email
    private void sendBookingEmail(User user, Event event, String ticketType, int quantity) {
        String subject = "Event Ticket Booking Confirmation";
        String emailBody = "<h2>Your event ticket has been successfully booked!</h2>"
                + "<p><b>Event:</b> " + event.getName() + "</p>"
                + "<p><b>Date:</b> " + event.getDate() + "</p>"
                + "<p><b>Time:</b> " + event.getTime() + "</p>"
                + "<p><b>Location:</b> " + event.getLocation() + "</p>"
                + "<p><b>Ticket Type:</b> " + ticketType + "</p>"
                + "<p><b>Quantity:</b> " + quantity + "</p>"
                + "<br><p>Thank you for booking!</p>";

        emailService.sendEmail(user.getEmail(), subject, emailBody);
    }

    // ✅ Send booking cancellation email
    private void sendCancellationEmail(User user, Event event) {
        String subject = "Event Ticket Cancellation";
        String emailBody = "<h2>Your event ticket booking has been canceled.</h2>"
                + "<p><b>Event:</b> " + event.getName() + "</p>"
                + "<p><b>Date:</b> " + event.getDate() + "</p>"
                + "<p><b>Time:</b> " + event.getTime() + "</p>"
                + "<p><b>Location:</b> " + event.getLocation() + "</p>"
                + "<br><p>We hope to see you at future events!</p>";

        emailService.sendEmail(user.getEmail(), subject, emailBody);
    }
}



//import com.eventmanagement.entities.Event;
//import com.eventmanagement.entities.EventRegistration;
//import com.eventmanagement.entities.User;
//import com.eventmanagement.repositories.EventRegistrationRepository;
//import com.eventmanagement.repositories.EventRepository;
//import com.eventmanagement.repositories.UserRepository;
//import com.eventmanagement.services.EmailService;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.Optional;
//
//@Controller
//public class EventRegistrationController {
//
//    private final EventRepository eventRepository;
//    private final UserRepository userRepository;
//    private final EventRegistrationRepository registrationRepository;
//    private final EmailService emailService;
//
//    public EventRegistrationController(EventRepository eventRepository,
//                                       UserRepository userRepository,
//                                       EventRegistrationRepository registrationRepository,
//                                       EmailService emailService) {
//        this.eventRepository = eventRepository;
//        this.userRepository = userRepository;
//        this.registrationRepository = registrationRepository;
//        this.emailService = emailService;
//    }
//
//    @GetMapping("/events/register/{eventId}")
//    public String registerForEvent(@PathVariable Long eventId,
//                                   @AuthenticationPrincipal UserDetails userDetails,
//                                   RedirectAttributes redirectAttributes) {
//        if (userDetails == null) {
//            redirectAttributes.addFlashAttribute("error", "You must be logged in to register.");
//            return "redirect:/login";
//        }
//
//        Optional<Event> eventOpt = eventRepository.findById(eventId);
//        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
//
//        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "Event not found.");
//            return "redirect:/events";
//        }
//
//        Event event = eventOpt.get();
//        User user = userOpt.get();
//
//        // Prevent Organizers from registering for their own event
//        if (event.getOrganizer().getId().equals(user.getId())) {
//            redirectAttributes.addFlashAttribute("error", "Organizers cannot register for their own events.");
//            return "redirect:/events/" + eventId;
//        }
//
//        // Check if the user is already registered
//        if (registrationRepository.existsByEventAndUser(event, user)) {
//            redirectAttributes.addFlashAttribute("error", "You are already registered for this event!");
//            return "redirect:/events/" + eventId;
//        }
//
//        // Proceed with registration
//        EventRegistration registration = new EventRegistration();
//        registration.setEvent(event);
//        registration.setUser(user);
//        registrationRepository.save(registration);
//
//        // Send confirmation email
//        String subject = "Event Registration Confirmation";
//        String emailBody = "<h2>You have successfully registered for an event!</h2>"
//                + "<p><b>Event:</b> " + event.getName() + "</p>"
//                + "<p><b>Date:</b> " + event.getDate() + "</p>"
//                + "<p><b>Time:</b> " + event.getTime() + "</p>"
//                + "<p><b>Location:</b> " + event.getLocation() + "</p>"
//                + "<br><p>Thank you for registering!</p>";
//
//        emailService.sendEmail(user.getEmail(), subject, emailBody);
//
//        redirectAttributes.addFlashAttribute("success", "Successfully registered for the event! A confirmation email has been sent.");
//        return "redirect:/events/" + eventId;
//    }
//
//    @GetMapping("/events/unregister/{eventId}")
//    public String unregisterFromEvent(@PathVariable Long eventId,
//                                      @AuthenticationPrincipal UserDetails userDetails,
//                                      RedirectAttributes redirectAttributes) {
//        if (userDetails == null) {
//            redirectAttributes.addFlashAttribute("error", "You must be logged in to unregister.");
//            return "redirect:/login";
//        }
//
//        Optional<Event> eventOpt = eventRepository.findById(eventId);
//        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
//
//        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "Event not found.");
//            return "redirect:/events";
//        }
//
//        Event event = eventOpt.get();
//        User user = userOpt.get();
//
//        Optional<EventRegistration> registrationOpt = registrationRepository.findByUserAndEvent(user, event);
//
//        if (registrationOpt.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "You are not registered for this event.");
//            return "redirect:/events/" + eventId;
//        }
//
//        // Delete registration
//        registrationRepository.delete(registrationOpt.get());
//
//        // Send Unregistration Email
//        String subject = "Event Unregistration Confirmation";
//        String emailBody = "<h2>You have successfully unregistered from the event.</h2>"
//                + "<p><b>Event:</b> " + event.getName() + "</p>"
//                + "<p><b>Date:</b> " + event.getDate() + "</p>"
//                + "<p><b>Time:</b> " + event.getTime() + "</p>"
//                + "<p><b>Location:</b> " + event.getLocation() + "</p>"
//                + "<br><p>We hope to see you at other events in the future!</p>";
//
//        emailService.sendEmail(user.getEmail(), subject, emailBody);
//
//        redirectAttributes.addFlashAttribute("success", "You have successfully unregistered from the event.");
//        return "redirect:/events/" + eventId;
//    }
//
//}