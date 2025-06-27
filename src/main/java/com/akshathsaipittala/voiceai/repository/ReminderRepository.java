package com.akshathsaipittala.voiceai.repository;

import com.akshathsaipittala.voiceai.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByActiveTrue();

    List<Reminder> findByActiveTrueAndCompletedFalse();

    @Query("SELECT r FROM Reminder r WHERE r.active = true AND r.completed = false AND r.reminderTime <= :now")
    List<Reminder> findDueReminders(LocalDateTime now);

    @Query("SELECT r FROM Reminder r WHERE r.active = true AND r.reminderTime BETWEEN :start AND :end")
    List<Reminder> findRemindersBetween(LocalDateTime start, LocalDateTime end);
}
