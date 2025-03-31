package com.eventmanagement.controllers;

import com.eventmanagement.entities.Event;
import com.eventmanagement.entities.EventRegistration;
import com.eventmanagement.entities.Role;
import com.eventmanagement.entities.User;
import com.eventmanagement.repositories.EventRegistrationRepository;
import com.eventmanagement.repositories.EventRepository;
import com.eventmanagement.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository eventRegistrationRepository;


    public EventController(EventRepository eventRepository, UserRepository userRepository, EventRegistrationRepository eventRegistrationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    // ✅ Show all events (Everyone can view)
    @GetMapping("/events")
    public String listEvents(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventRepository.findAll(pageable);

        model.addAttribute("eventPage", eventPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventPage.getTotalPages());

        return "events/list";
    }

    // ✅ ORGANIZER can create events
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @GetMapping("/events/create")
    public String showCreateForm(Model model) {
        model.addAttribute("event", new Event());
        return "events/create";
    }

    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @PostMapping("/events/create")
    public String createEvent(@ModelAttribute Event event, @AuthenticationPrincipal UserDetails userDetails) {
        User organizer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        event.setOrganizer(organizer);
        eventRepository.save(event);
        return "redirect:/events";
    }

    // ✅ Edit Event (Only Admin & Owner)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    @GetMapping("/events/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals(Role.ADMIN) && !event.getOrganizer().getId().equals(user.getId())) {
            return "redirect:/events?error=Unauthorized";
        }

        model.addAttribute("event", event);
        return "events/edit";
    }

    // ✅ Process Event Update
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    @PostMapping("/events/edit/{id}")
    public String updateEvent(@PathVariable Long id, @ModelAttribute Event updatedEvent, @AuthenticationPrincipal UserDetails userDetails) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals(Role.ADMIN) && !event.getOrganizer().getId().equals(user.getId())) {
            return "redirect:/events?error=Unauthorized";
        }

        event.setName(updatedEvent.getName());
        event.setDescription(updatedEvent.getDescription());
        event.setDate(updatedEvent.getDate());
        event.setTime(updatedEvent.getTime());
        event.setLocation(updatedEvent.getLocation());

        eventRepository.save(event);
        return "redirect:/events";
    }


    @GetMapping("/events/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER','ROLE_ADMIN', 'ROLE_ATTENDEE')")
    public String showEventDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        if (eventOptional.isEmpty()) {
            return "events/404";
        }

        Event event = eventOptional.get();
        model.addAttribute("event", event);
        model.addAttribute("registrations", event.getRegistrations());

        // ✅ Check if the user is the event organizer
        boolean isOrganizer = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(userDetails.getUsername());
        model.addAttribute("isOrganizer", isOrganizer);

        // ✅ Check if the logged-in user is registered
        boolean isRegistered = false;
        if (userDetails != null) {
            Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
            if (userOpt.isPresent()) {
                isRegistered = eventRegistrationRepository.existsByEventAndUser(event, userOpt.get());

            }
        }
        model.addAttribute("isRegistered", isRegistered);

        return "events/details";
    }


//    // ✅ Show Event Details (Fetch Registrations)
//    @GetMapping("/events/{id}")
//    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER','ROLE_ADMIN', 'ROLE_ATTENDEE')")
//    public String showEventDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
//        Optional<Event> eventOptional = eventRepository.findById(id);
//        if (eventOptional.isEmpty()) {
//            return "events/404";
//        }
//
//        Event event = eventOptional.get();
//        model.addAttribute("event", event);
//        model.addAttribute("registrations", event.getRegistrations());
//
//        // ✅ Check if user is the event organizer
//        boolean isOrganizer = event.getOrganizer() != null && event.getOrganizer().getUsername().equals(userDetails.getUsername());
//        model.addAttribute("isOrganizer", isOrganizer);
//
//        return "events/details";
//    }

    @PostMapping("/events/mark-attendance")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public String markAttendance(@RequestParam Long eventId,
                                 @RequestParam(required = false) List<Long> attendedUsers,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            User loggedInUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Check if the logged-in user is the Admin OR the Event Organizer
            boolean isAdmin = loggedInUser.getRole().equals(Role.ADMIN);
            boolean isOrganizer = event.getOrganizer() != null &&
                    event.getOrganizer().getUsername().equals(userDetails.getUsername());


            System.out.println("🔹 Logged-in user: " + loggedInUser.getUsername());
            System.out.println("🔹 Logged-in user role: " + loggedInUser.getRole());
            System.out.println("🔹 Event organizer: " + (event.getOrganizer() != null ? event.getOrganizer().getUsername() : "No Organizer"));

            if (isAdmin || isOrganizer) {
                // ✅ Update attendance for registered users
                List<EventRegistration> registrations = event.getRegistrations();
                for (EventRegistration registration : registrations) {
                    registration.setAttended(attendedUsers != null && attendedUsers.contains(registration.getUser().getId()));
                    eventRegistrationRepository.save(registration);
                }

                return "redirect:/events/" + eventId + "?success=AttendanceUpdated";
            } else {
                return "redirect:/events/" + eventId + "?error=Unauthorized";
            }
        }

        return "redirect:/events?error=EventNotFound";
    }


    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    @GetMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Event> eventOptional = eventRepository.findById(id);

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Only allow deletion if the user is ADMIN or the event organizer
            if (user.getRole().equals(Role.ADMIN) || event.getOrganizer().getId().equals(user.getId())) {
                eventRepository.delete(event);
                return "redirect:/events?success=EventDeleted";
            } else {
                return "redirect:/events?error=Unauthorized";
            }
        }

        return "redirect:/events?error=EventNotFound";
    }

    @GetMapping("/events/search")
    public String searchEvents(@RequestParam(value = "query", required = false) String query,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        Page<Event> eventPage;

        Pageable pageable = PageRequest.of(page, 5); // ✅ Adjust page size as needed

        if (query != null && !query.trim().isEmpty()) {
            eventPage = eventRepository.findByNameContainingIgnoreCase(query, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }

        model.addAttribute("eventPage", eventPage); // ✅ Matches list.html
        model.addAttribute("query", query); // ✅ To keep search text in input field
        return "events/list";
    }








    @GetMapping("/events/filter")
    public String filterEvents(@RequestParam("type") String type, Model model) {
        List<Event> events;

        // Get today's date as a String in your format
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if ("upcoming".equals(type)) {
            events = eventRepository.findByDateAfter(todayDate);
        } else if ("past".equals(type)) {
            events = eventRepository.findByDateBefore(todayDate);
        } else {
            events = eventRepository.findAll();
        }

        model.addAttribute("eventPage", new PageImpl<>(events)); // Ensure it works with pagination
        model.addAttribute("filterType", type); // ✅ This makes the button active in HTML
        return "events/list";
    }








}
