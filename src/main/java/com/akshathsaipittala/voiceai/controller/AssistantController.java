package com.akshathsaipittala.voiceai.controller;

import com.akshathsaipittala.voiceai.service.*;
import com.akshathsaipittala.voiceai.model.Conversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final TextToSpeechService textToSpeechService;

    @Value("${voice.assistant.wake-word}")
    private String wakeWord;

    @Autowired
    public AssistantController(
            AssistantService assistantService,
            TextToSpeechService textToSpeechService) {
        this.assistantService = assistantService;
        this.textToSpeechService = textToSpeechService;
    }

    @PostMapping("/start")
    public Map<String, Object> startAssistant() {
        assistantService.start();
        return Map.of(
            "status", "started",
            "message", "Voice assistant is now running",
            "wakeWord", wakeWord
        );
    }

    @PostMapping("/stop")
    public Map<String, Object> stopAssistant() {
        assistantService.stop();
        return Map.of(
            "status", "stopped",
            "message", "Voice assistant has been stopped"
        );
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return Map.of(
            "running", assistantService.isRunning(),
            "listening", assistantService.isListening(),
            "wakeWord", wakeWord,
            "ttsAvailable", textToSpeechService.isAvailable()
        );
    }

    @PostMapping("/command")
    public Map<String, Object> processCommand(@RequestBody Map<String, String> request) {
        String command = request.get("command");
        if (command == null || command.trim().isEmpty()) {
            return Map.of(
                "error", "Command cannot be empty",
                "response", ""
            );
        }

        String response = assistantService.processCommand(command);
        return Map.of(
            "command", command,
            "response", response,
            "status", "processed"
        );
    }

    @PostMapping("/speak")
    public Map<String, Object> speak(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.trim().isEmpty()) {
            return Map.of("error", "Text cannot be empty");
        }

        textToSpeechService.speakAsync(text);
        return Map.of(
            "status", "speaking",
            "text", text
        );
    }

    @GetMapping("/conversations")
    public List<Conversation> getRecentConversations() {
        return assistantService.getRecentConversations();
    }
}
