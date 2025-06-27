package com.akshathsaipittala.voiceai.service;

import com.akshathsaipittala.voiceai.model.Conversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);
    private final WakeWordService wakeWordService;
    private final SpeechToTextService speechToTextService;
    private final TextToSpeechService textToSpeechService;
    private final AIService aiService;
    private final NetworkService networkService;

    private final List<Conversation> conversationHistory = new ArrayList<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isListening = new AtomicBoolean(false);

    @Autowired
    public AssistantService(WakeWordService wakeWordService,
                           SpeechToTextService speechToTextService,
                           TextToSpeechService textToSpeechService,
                           AIService aiService,
                           NetworkService networkService) {
        this.wakeWordService = wakeWordService;
        this.speechToTextService = speechToTextService;
        this.textToSpeechService = textToSpeechService;
        this.aiService = aiService;
        this.networkService = networkService;
    }

    public void start() {
        if (isRunning.get()) {
            logger.info("Assistant already running");
            return;
        }

        logger.info("Starting voice assistant...");
        isRunning.set(true);

        // Initialize wake word service
        wakeWordService.initialize();

        // Announce startup
        String startupMessage = "Voice assistant is ready. " + networkService.getNetworkStatus();
        textToSpeechService.speakAsync(startupMessage);
        logger.info("Assistant started successfully");

        // Start the main listening loop
        startListeningLoop();
    }

    public void stop() {
        if (!isRunning.get()) {
            return;
        }

        logger.info("Stopping voice assistant...");
        isRunning.set(false);
        isListening.set(false);

        wakeWordService.stopListening();
        textToSpeechService.speakAsync("Voice assistant is shutting down.");
        logger.info("Assistant stopped");
    }

    private void startListeningLoop() {
        CompletableFuture.runAsync(() -> {
            // Check if wake word detection is available
            if (!wakeWordService.isWakeWordDetectionAvailable()) {
                logger.info("Wake word detection not available. Assistant will run in manual mode.");
                logger.info("You can still use the assistant through the web interface or CLI commands.");
                return;
            }

            while (isRunning.get()) {
                try {
                    // Wait for wake word
                    logger.debug("Listening for wake word...");
                    wakeWordService.startListening();

                    // Check for wake word detection
                    if (wakeWordService.isWakeWordDetected()) {
                        handleWakeWordDetection();
                    }

                    // Brief pause to prevent excessive CPU usage
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in listening loop: {}", e.getMessage());
                    try {
                        Thread.sleep(1000); // Wait before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    private void handleWakeWordDetection() {
        logger.info("Wake word detected, processing voice command...");
        isListening.set(true);

        try {
            // Acknowledge wake word
            textToSpeechService.speak("Yes?");

            // Listen for command
            String spokenText = speechToTextService.transcribeAudio();

            if (spokenText != null && !spokenText.trim().isEmpty()) {
                logger.info("User said: {}", spokenText);

                // Process the command
                String response = processCommand(spokenText);

                // Speak the response
                if (response != null && !response.trim().isEmpty()) {
                    textToSpeechService.speak(response);
                }

                // Log the conversation
                logConversation(spokenText, response);

            } else {
                textToSpeechService.speak("I didn't catch that. Please try again.");
            }

        } catch (Exception e) {
            logger.error("Error processing voice command: {}", e.getMessage());
            textToSpeechService.speak("Sorry, I had trouble processing that command.");
        } finally {
            isListening.set(false);
        }
    }

    public String processCommand(String command) {
        try {
            logger.debug("Processing command: {}", command);

            // Handle system commands first
            if (isSystemCommand(command)) {
                return handleSystemCommand(command);
            }

            // Process through AI service
            String response = aiService.processQuery(command);

            if (response == null || response.trim().isEmpty()) {
                response = "I'm not sure how to help with that. Could you try rephrasing your question?";
            }

            return response;

        } catch (Exception e) {
            logger.error("Error processing command '{}': {}", command, e.getMessage());
            return "Sorry, I encountered an error processing your request.";
        }
    }

    private boolean isSystemCommand(String command) {
        String lowerCommand = command.toLowerCase();
        return lowerCommand.contains("stop assistant") ||
               lowerCommand.contains("shutdown") ||
               lowerCommand.contains("quit") ||
               lowerCommand.contains("status") ||
               lowerCommand.contains("help");
    }

    private String handleSystemCommand(String command) {
        String lowerCommand = command.toLowerCase();

        if (lowerCommand.contains("stop") || lowerCommand.contains("shutdown") || lowerCommand.contains("quit")) {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000); // Allow response to be spoken
                    stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return "Goodbye!";
        } else if (lowerCommand.contains("status")) {
            return getSystemStatus();
        } else if (lowerCommand.contains("help")) {
            return getHelpMessage();
        }

        return "I didn't understand that system command.";
    }

    private String getSystemStatus() {
        StringBuilder status = new StringBuilder();
        status.append("System status: ");
        status.append(isRunning.get() ? "Running. " : "Stopped. ");
        status.append(networkService.getNetworkStatus()).append(". ");
        status.append("Text-to-speech: ").append(textToSpeechService.isAvailable() ? "Available" : "Unavailable");
        return status.toString();
    }

    private String getHelpMessage() {
        return "I can help you with reminders, coding questions, nutrition tracking, weather, news, and general questions. " +
               "Try saying things like 'remind me to call John at 3 PM', 'what's the weather like', or 'log my breakfast'.";
    }

    private void logConversation(String userInput, String response) {
        try {
            Conversation conversation = new Conversation();
            conversation.setUserInput(userInput);
            conversation.setAssistantResponse(response);
            conversation.setTimestamp(LocalDateTime.now());
            conversationHistory.add(conversation);

            // Keep only last 50 conversations in memory
            if (conversationHistory.size() > 50) {
                conversationHistory.remove(0);
            }

        } catch (Exception e) {
            logger.error("Error logging conversation: {}", e.getMessage());
        }
    }

    public List<Conversation> getRecentConversations() {
        return new ArrayList<>(conversationHistory);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean isListening() {
        return isListening.get();
    }
}