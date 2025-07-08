package com.akshathsaipittala.voiceai;

import com.akshathsaipittala.voiceai.weather.WeatherConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(WeatherConfigProperties.class)
public class VoiceAiApplication {

    public static void main(String[] args) {
        System.out.println("🎤 Starting Voice AI Assistant...");
        SpringApplication.run(VoiceAiApplication.class, args);
    }
}
