package com.akshathsaipittala.voiceai.service;

import com.akshathsaipittala.voiceai.model.Reminder;
import com.akshathsaipittala.voiceai.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private final ReminderRepository reminderRepository;
    private final TextToSpeechService textToSpeechService;

    @Autowired
    public ReminderService(ReminderRepository reminderRepository, TextToSpeechService textToSpeechService) {
        this.reminderRepository = reminderRepository;
        this.textToSpeechService = textToSpeechService;
    }

    public Reminder createReminder(String text, LocalDateTime reminderTime) {
        Reminder reminder = new Reminder(text, reminderTime);
        Reminder saved = reminderRepository.save(reminder);
        logger.info("Created reminder: {} at {}", text, reminderTime);
        return saved;
    }

    public List<Reminder> getActiveReminders() {
        return reminderRepository.findByActiveTrueAndCompletedFalse();
    }

    public List<Reminder> getAllReminders() {
        return reminderRepository.findByActiveTrue();
    }

    public boolean deleteReminder(Long id) {
        Optional<Reminder> reminder = reminderRepository.findById(id);
        if (reminder.isPresent()) {
            Reminder r = reminder.get();
            r.setActive(false);
            reminderRepository.save(r);
            logger.info("Deleted reminder: {}", r.getText());
            return true;
        }
        return false;
    }

    public boolean completeReminder(Long id) {
        Optional<Reminder> reminder = reminderRepository.findById(id);
        if (reminder.isPresent()) {
            Reminder r = reminder.get();
            r.setCompleted(true);
            reminderRepository.save(r);
            logger.info("Completed reminder: {}", r.getText());
            return true;
        }
        return false;
    }

    public List<Reminder> getTodaysReminders() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return reminderRepository.findRemindersBetween(startOfDay, endOfDay);
    }

    /**
     * Check for due reminders every minute and announce them
     */
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkDueReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Reminder> dueReminders = reminderRepository.findDueReminders(now);

            for (Reminder reminder : dueReminders) {
                if (!reminder.isCompleted()) {
                    announceReminder(reminder);
                    // Mark as completed so it doesn't repeat
                    completeReminder(reminder.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error checking due reminders: {}", e.getMessage());
        }
    }

    private void announceReminder(Reminder reminder) {
        String announcement = "Reminder: " + reminder.getText();
        logger.info("Announcing reminder: {}", announcement);

        if (textToSpeechService.isAvailable()) {
            textToSpeechService.speakAsync(announcement);
        } else {
            // Fallback to console output if TTS is not available
            System.out.println("🔔 " + announcement);
        }
    }
}
