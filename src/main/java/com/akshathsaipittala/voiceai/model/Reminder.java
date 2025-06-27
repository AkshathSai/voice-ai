package com.akshathsaipittala.voiceai.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_completed")
    private boolean completed = false;

    @Column(name = "is_active")
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Reminder(String text, LocalDateTime reminderTime) {
        this.text = text;
        this.reminderTime = reminderTime;
        this.createdAt = LocalDateTime.now();
        this.completed = false;
        this.active = true;
    }
}
