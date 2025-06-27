package com.akshathsaipittala.voiceai.service;

import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

@Service
public class SpeechToTextService {

    private Object model;
    private boolean voskAvailable = false;
    private boolean voskInitialized = false;
    private static final String MODEL_PATH = "models/vosk-model-small-en-us-0.15";

    public SpeechToTextService() {
        // Don't initialize Vosk in constructor to avoid startup failures
        System.out.println("SpeechToTextService created. Vosk will be initialized on first use.");
    }

    private void initializeVosk() {
        if (voskInitialized) {
            return;
        }

        voskInitialized = true;
        try {
            // Dynamically load Vosk classes to avoid static initialization issues
            Class<?> libVoskClass = Class.forName("org.vosk.LibVosk");
            Class<?> logLevelClass = Class.forName("org.vosk.LogLevel");
            Class<?> modelClass = Class.forName("org.vosk.Model");

            // Set log level
            Object warningsLevel = logLevelClass.getField("WARNINGS").get(null);
            libVoskClass.getMethod("setLogLevel", logLevelClass).invoke(null, warningsLevel);

            // Create model
            model = modelClass.getConstructor(String.class).newInstance(MODEL_PATH);
            voskAvailable = true;
            System.out.println("Vosk model initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize VOSK model: " + e.getMessage());
            System.err.println("Speech-to-text functionality will be disabled");
            voskAvailable = false;
        }
    }

    public String transcribeAudio() {
        initializeVosk();

        if (!voskAvailable) {
            System.err.println("Vosk is not available. Speech-to-text disabled.");
            return "Speech recognition not available";
        }

        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line not supported");
                return "Audio input not supported";
            }

            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("Listening...");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];

            // Use reflection to create Recognizer
            Class<?> recognizerClass = Class.forName("org.vosk.Recognizer");
            Object recognizer = recognizerClass.getConstructor(model.getClass(), int.class)
                    .newInstance(model, 16000);

            long startTime = System.currentTimeMillis();
            boolean done = false;

            while (!done && System.currentTimeMillis() - startTime < 5000) {
                int numBytesRead = line.read(buffer, 0, buffer.length);
                out.write(buffer, 0, numBytesRead);

                boolean accepted = (Boolean) recognizerClass.getMethod("acceptWaveForm", byte[].class, int.class)
                        .invoke(recognizer, buffer, numBytesRead);

                if (accepted) {
                    String result = (String) recognizerClass.getMethod("getResult").invoke(recognizer);
                    if (result.contains("text")) {
                        line.stop();
                        line.close();
                        return extractText(result);
                    }
                }
            }

            line.stop();
            line.close();

            String finalResult = (String) recognizerClass.getMethod("getFinalResult").invoke(recognizer);
            recognizerClass.getMethod("close").invoke(recognizer);

            return extractText(finalResult);

        } catch (Exception e) {
            System.err.println("Error in speech recognition: " + e.getMessage());
            return "Error in speech recognition";
        }
    }

    public boolean isVoskAvailable() {
        initializeVosk();
        return voskAvailable;
    }

    private String extractText(String jsonResult) {
        // Simple extraction - in real implementation, use a proper JSON parser
        int startIndex = jsonResult.indexOf("\"text\" : \"") + 10;
        int endIndex = jsonResult.indexOf("\"", startIndex);
        if (startIndex > 9 && endIndex > startIndex) {
            return jsonResult.substring(startIndex, endIndex);
        }
        return "";
    }
}