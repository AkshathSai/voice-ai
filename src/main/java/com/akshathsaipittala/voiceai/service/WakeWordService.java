package com.akshathsaipittala.voiceai.service;

import ai.picovoice.porcupine.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class WakeWordService {

    private static final Logger logger = LoggerFactory.getLogger(WakeWordService.class);

    @Value("${voice.assistant.wake-word}")
    private String wakeWordPhrase;

    @Value("${porcupine.access-key:}")
    private String porcupineAccessKey;

    @Value("${porcupine.enabled:false}")
    private boolean porcupineEnabled;

    private Porcupine porcupine;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean wakeWordDetected = new AtomicBoolean(false);
    private TargetDataLine microphone;
    private boolean isInitialized = false;

    public void initialize() {
        try {
            // Check if Porcupine is enabled and access key is provided
            if (!porcupineEnabled || porcupineAccessKey == null || porcupineAccessKey.trim().isEmpty()) {
                logger.warn("Porcupine wake word detection is disabled or access key not provided. Wake word detection will not be available.");
                logger.info("To enable wake word detection, set porcupine.enabled=true and provide a valid PORCUPINE_ACCESS_KEY environment variable.");
                isInitialized = false;
                return;
            }

            // Initialize Porcupine with access key
            porcupine = new Porcupine.Builder()
                    .setAccessKey(porcupineAccessKey)
                    .setBuiltInKeyword(Porcupine.BuiltInKeyword.ALEXA) // Using built-in keyword
                    .build();

            isInitialized = true;
            logger.info("Wake word detection initialized successfully with keyword: {}", wakeWordPhrase);
        } catch (PorcupineException e) {
            logger.error("Error initializing wake word detector: {}", e.getMessage());
            logger.warn("Wake word detection will be disabled. You can continue using the assistant without wake word functionality.");
            isInitialized = false;
        }
    }

    public void startListening() {
        if (!isInitialized) {
            logger.debug("Wake word detection not initialized, skipping...");
            return;
        }

        if (isRunning.get()) {
            return;
        }

        isRunning.set(true);
        wakeWordDetected.set(false);

        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                logger.error("Line not supported for wake word detection");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            logger.debug("Wake word listening started");

            byte[] buffer = new byte[porcupine.getFrameLength() * 2];
            short[] audioData = new short[porcupine.getFrameLength()];

            while (isRunning.get() && !wakeWordDetected.get()) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                if (bytesRead > 0) {
                    // Convert byte array to short array
                    ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

                    try {
                        int keywordIndex = porcupine.process(audioData);
                        if (keywordIndex >= 0) {
                            logger.info("Wake word detected!");
                            wakeWordDetected.set(true);
                            break;
                        }
                    } catch (PorcupineException e) {
                        logger.error("Error processing audio for wake word: {}", e.getMessage());
                        break;
                    }
                }
            }

        } catch (LineUnavailableException e) {
            logger.error("Microphone unavailable for wake word detection: {}", e.getMessage());
        } finally {
            stopListening();
        }
    }

    public void stopListening() {
        isRunning.set(false);
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
        }
        logger.debug("Wake word listening stopped");
    }

    public boolean isWakeWordDetected() {
        return wakeWordDetected.get();
    }

    public void resetWakeWordDetection() {
        wakeWordDetected.set(false);
    }

    public boolean isWakeWordDetectionAvailable() {
        return isInitialized;
    }

    public void cleanup() {
        stopListening();
        if (porcupine != null) {
            porcupine.delete();
        }
    }
}