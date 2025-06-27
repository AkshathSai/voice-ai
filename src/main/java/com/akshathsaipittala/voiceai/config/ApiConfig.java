package com.akshathsaipittala.voiceai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiConfig {

    @Value("${weather.api.base-url}")
    private String weatherApiBaseUrl;

    @Value("${news.api.base-url}")
    private String newsApiBaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    @Bean
    public WebClient weatherWebClient() {
        return WebClient.builder()
                .baseUrl(weatherApiBaseUrl)
                .build();
    }

    @Bean
    public WebClient newsWebClient() {
        return WebClient.builder()
                .baseUrl(newsApiBaseUrl)
                .build();
    }
}
