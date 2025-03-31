package com.eventmanagement.controllers;

import com.eventmanagement.entities.User;
import com.eventmanagement.entities.Role;
import com.eventmanagement.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth") // ✅ Added a base path for better organization
public class RegisterController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Show the registration page
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // ✅ Ensure register.html exists in templates
    }

    @PostMapping("/register")
    public String processFormRegistration(@RequestParam String username,
                                          @RequestParam String email,
                                          @RequestParam String password,
                                          @RequestParam String role,
                                          Model model) {
        // Check if the username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists!");
            return "register"; // Show error message in register page
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        try {
            user.setRole(Role.valueOf(role.toUpperCase())); // Convert role string to ENUM
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid role selected!");
            return "register";
        }

        userRepository.save(user);

        // Redirect to login with success message
        return "redirect:/login?success=true";
    }




}
