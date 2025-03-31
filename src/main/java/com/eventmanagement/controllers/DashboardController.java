package com.eventmanagement.controllers;

import com.eventmanagement.entities.Event;
import com.eventmanagement.entities.EventRegistration;
import com.eventmanagement.entities.Role;
import com.eventmanagement.entities.User;

import com.eventmanagement.repositories.EventRegistrationRepository;
import com.eventmanagement.repositories.EventRepository;
import com.eventmanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (user.getRole() == Role.ADMIN) {  // ✅ Compare with Enum
            System.out.println("this is ADMIN");
            return adminDashboard(model);
        } else if (user.getRole() == Role.ORGANIZER) {
            System.out.println("this is ORGANIZER");
            return organizerDashboard(user, model);
        } else {
            System.out.println("this is attendee");
            return attendeeDashboard(user, model);

        }

    }

    private String adminDashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalEvents", eventRepository.count());
        model.addAttribute("totalRegistrations", eventRegistrationRepository.count());
        return "dashboard/admin-dashboard";
    }

    private String organizerDashboard(User organizer, Model model) {
        List<Event> myEvents = eventRepository.findByOrganizer(organizer);
        model.addAttribute("myEvents", myEvents);
        return "dashboard/organizer-dashboard";
    }

    private String attendeeDashboard(User user, Model model) {
        List<EventRegistration> myRegistrations = eventRegistrationRepository.findByUser(user);
        model.addAttribute("myRegistrations", myRegistrations);
        return "dashboard/attendee-dashboard";
    }

}
