package com.eventmanagement.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private boolean attended; // Track attendance
    private LocalDateTime registrationDate; //  Registration date

    private String ticketType; //  VIP, General, etc.
    private int quantity; //  Number of tickets booked
    private String status; //  CONFIRMED, PENDING, CANCELLED

    @PrePersist
    protected void onRegister() {
        this.registrationDate = LocalDateTime.now(); //  Set default registration date
    }
}
