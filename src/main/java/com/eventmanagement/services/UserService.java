package com.eventmanagement.services;

import com.eventmanagement.entities.User;
import com.eventmanagement.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void updateUserProfile(String username, String email, String password, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username).orElseThrow();

        user.setEmail(email);
        if (!password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        if (!file.isEmpty()) {
            //  Use absolute path for proper storage
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs(); // Create the directory if it doesn't exist
            }

            //  Sanitize file name (remove spaces)
            String filename = username + "_" + file.getOriginalFilename().replaceAll(" ", "_");
            File destination = new File(uploadDir + filename);
            file.transferTo(destination);

            //  Save only the relative path in the database
            user.setProfileImage(filename);
        }

        userRepository.save(user);
    }

}
