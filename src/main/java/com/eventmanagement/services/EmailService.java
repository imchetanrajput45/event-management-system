package com.eventmanagement.services;



import java.time.LocalDate;
import java.util.List;

import com.eventmanagement.entities.Event;
import com.eventmanagement.entities.EventRegistration;
import com.eventmanagement.repositories.EventRegistrationRepository;
import com.eventmanagement.repositories.EventRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    public EmailService(JavaMailSender mailSender, EventRepository eventRepository, EventRegistrationRepository registrationRepository) {
        this.mailSender = mailSender;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("chetan1542003@gmail.com"); // ✅ Use a verified sender email
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // ✅ Enable HTML content

            mailSender.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Error sending email: " + e.getMessage());
        }
    }


    // ✅ Scheduler to send reminders for events happening tomorrow
    @Scheduled(cron = "0 0 9 * * ?") // Runs every day at 9 AM
    public void sendEventReminders() {
        System.out.println("Running scheduled task: Sending event reminders...");

        String tomorrow = LocalDate.now().plusDays(1).toString(); // 📌 Get tomorrow's date as a String
        List<Event> upcomingEvents = eventRepository.findByDate(tomorrow); // 📌 Find events happening tomorrow

        for (Event event : upcomingEvents) {
            List<EventRegistration> registrations = registrationRepository.findByEvent(event);

            for (EventRegistration registration : registrations) {
                String userEmail = registration.getUser().getEmail();
                String subject = "Reminder: Upcoming Event - " + event.getName();
                String emailBody = "<h2>Reminder: Your Event is Tomorrow!</h2>"
                        + "<p><b>Event:</b> " + event.getName() + "</p>"
                        + "<p><b>Date:</b> " + event.getDate() + "</p>"
                        + "<p><b>Time:</b> " + event.getTime() + "</p>"
                        + "<p><b>Location:</b> " + event.getLocation() + "</p>"
                        + "<br><p>We look forward to seeing you there!</p>";

                sendEmail(userEmail, subject, emailBody);
            }
        }

        System.out.println("Event reminder emails sent successfully!");
    }
}
