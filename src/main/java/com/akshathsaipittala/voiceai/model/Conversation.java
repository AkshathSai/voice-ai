package com.akshathsaipittala.voiceai.model;

import java.time.LocalDateTime;

public class Conversation {

    private String userInput;
    private String assistantResponse;
    private LocalDateTime timestamp;

    public Conversation() {
        this.timestamp = LocalDateTime.now();
    }

    public Conversation(String userInput, String assistantResponse) {
        this.userInput = userInput;
        this.assistantResponse = assistantResponse;
        this.timestamp = LocalDateTime.now();
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public String getAssistantResponse() {
        return assistantResponse;
    }

    public void setAssistantResponse(String assistantResponse) {
        this.assistantResponse = assistantResponse;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
