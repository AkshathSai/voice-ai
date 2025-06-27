package com.akshathsaipittala.voiceai.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "daily_calorie_goal")
    private Integer dailyCalorieGoal;

    @Column(name = "preferred_units")
    private String preferredUnits;

    @Column(name = "wake_word")
    private String wakeWord;

    @Column(name = "voice_enabled")
    private Boolean voiceEnabled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UserProfile(String userName) {
        this.userName = userName;
        this.dailyCalorieGoal = 2000;
        this.preferredUnits = "metric";
        this.wakeWord = "hey assistant";
        this.voiceEnabled = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
