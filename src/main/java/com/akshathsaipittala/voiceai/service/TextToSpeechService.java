package com.akshathsaipittala.voiceai.service;

import org.springframework.stereotype.Service;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TextToSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(TextToSpeechService.class);
    private Voice voice;
    private final ExecutorService speechExecutor = Executors.newSingleThreadExecutor();
    private boolean isInitialized = false;

    @PostConstruct
    public void initialize() {
        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            VoiceManager voiceManager = VoiceManager.getInstance();
            voice = voiceManager.getVoice("kevin16");

            if (voice != null) {
                voice.allocate();
                // Configure voice for better quality on M1 Mac
                voice.setRate(150);  // Words per minute
                voice.setPitch(100); // Pitch
                voice.setVolume(0.8f); // Volume
                isInitialized = true;
                logger.info("Text-to-Speech initialized successfully with Kevin voice");
            } else {
                logger.error("Cannot find Kevin voice - TTS will be disabled");
            }
        } catch (Exception e) {
            logger.error("Error initializing Text-to-Speech: {}", e.getMessage());
        }
    }

    public CompletableFuture<Void> speakAsync(String text) {
        if (!isInitialized || voice == null) {
            logger.warn("TTS not initialized - cannot speak text: {}", text);
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Speaking: {}", text);
                voice.speak(text);
            } catch (Exception e) {
                logger.error("Error while speaking: {}", e.getMessage());
            }
        }, speechExecutor);
    }

    public void speak(String text) {
        if (!isInitialized || voice == null) {
            logger.warn("TTS not initialized - cannot speak text: {}", text);
            return;
        }

        try {
            logger.debug("Speaking: {}", text);
            voice.speak(text);
        } catch (Exception e) {
            logger.error("Error while speaking: {}", e.getMessage());
        }
    }

    public boolean isAvailable() {
        return isInitialized && voice != null;
    }

    @PreDestroy
    public void cleanup() {
        speechExecutor.shutdown();
        if (voice != null) {
            voice.deallocate();
            logger.info("Text-to-Speech service cleaned up");
        }
    }
}