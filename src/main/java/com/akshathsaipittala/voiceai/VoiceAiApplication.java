package com.akshathsaipittala.voiceai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VoiceAiApplication {

    public static void main(String[] args) {
        System.out.println("🎤 Starting Voice AI Assistant...");
        SpringApplication.run(VoiceAiApplication.class, args);
    }
}
