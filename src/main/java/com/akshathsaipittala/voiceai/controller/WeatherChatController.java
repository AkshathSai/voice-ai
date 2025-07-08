package com.akshathsaipittala.voiceai.controller;

import com.akshathsaipittala.voiceai.weather.WeatherConfigProperties;
import com.akshathsaipittala.voiceai.weather.WeatherRequest;
import com.akshathsaipittala.voiceai.weather.WeatherService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherChatController {

    private final ChatClient chatClient;

    public WeatherChatController(WeatherConfigProperties props,
                                 ChatClient.Builder chatClient) {

        ToolCallback toolCallback = FunctionToolCallback
                .builder("currentWeather", new WeatherService(props))
                .description("Get the weather in location")
                .inputType(WeatherRequest.class)
                .build();

        this.chatClient = chatClient.defaultToolCallbacks(toolCallback)
                .build();
    }


    @GetMapping("/weather/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .system("You are a helpful assistant.")
                .user(message)
                .call()
                .content();
    }

}
