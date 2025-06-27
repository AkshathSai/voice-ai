package com.akshathsaipittala.voiceai.service;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final OllamaApi ollamaApi;
    private final WebSearchService webSearchService;
    private final WeatherService weatherService;
    private final SportsService sportsService;
    private final List<Message> conversationHistory = new ArrayList<>();

    @Value("${spring.ai.ollama.model}")
    private String model;

    // Patterns for detecting specific query types
    private static final Pattern WEATHER_PATTERN = Pattern.compile(
        "\\b(weather|temperature|forecast|rain|snow|sunny|cloudy|humid)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SPORTS_PATTERN = Pattern.compile(
        "\\b(sports?|score|game|match|football|basketball|baseball|hockey|soccer|nfl|nba|mlb|nhl|mls)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LOCATION_PATTERN = Pattern.compile(
        "\\bin\\s+([a-zA-Z\\s]+?)(?:\\s|$|\\?|\\.|,)",
        Pattern.CASE_INSENSITIVE
    );

    @Autowired
    public AIService(OllamaApi ollamaApi, WebSearchService webSearchService,
                     WeatherService weatherService, SportsService sportsService) {
        this.ollamaApi = ollamaApi;
        this.webSearchService = webSearchService;
        this.weatherService = weatherService;
        this.sportsService = sportsService;
        initializeAssistant();
    }

    private void initializeAssistant() {
        String systemPrompt = String.format("""
            You are a helpful AI voice assistant running on an M1 MacBook Pro with comprehensive internet access capabilities. Your responses should be:
            1. Concise and conversational - perfect for voice interactions
            2. Accurate and up-to-date using real-time data when provided
            3. Friendly and helpful
            4. Clear about your information sources

            You have access to:
            - Real-time web search results
            - Current weather information
            - Live sports scores and schedules
            - Breaking news and updates
            
            When provided with real-time data, always use it to give the most current information.
            Keep responses under 3-4 sentences for voice delivery unless specifically asked for details.
            Current date: %s
            """, java.time.LocalDate.now());

        Message systemMessage = Message.builder(Message.Role.SYSTEM)
                .content(systemPrompt)
                .build();
        conversationHistory.add(systemMessage);

        logger.info("AI Service initialized with model: {} and full internet access enabled", model);
    }

    public String processQuery(String query) {
        try {
            String enhancedQuery = query;
            StringBuilder contextBuilder = new StringBuilder();

            // Check for weather queries first (most specific)
            if (WEATHER_PATTERN.matcher(query).find()) {
                logger.info("Processing weather query: {}", query);
                String location = extractLocation(query);
                String weatherInfo = weatherService.getCurrentWeather(location);
                contextBuilder.append("CURRENT WEATHER DATA:\n").append(weatherInfo).append("\n\n");
            }

            // Check for sports queries
            else if (SPORTS_PATTERN.matcher(query).find()) {
                logger.info("Processing sports query: {}", query);
                String sport = extractSport(query);
                String sportsInfo;

                if (query.toLowerCase().contains("today") || query.toLowerCase().contains("schedule")) {
                    sportsInfo = sportsService.getTodaysGames(sport);
                } else {
                    sportsInfo = sportsService.getSportsScores(sport, null);
                }
                contextBuilder.append("CURRENT SPORTS DATA:\n").append(sportsInfo).append("\n\n");
            }

            // For other queries that need internet search
            else if (webSearchService.requiresInternetSearch(query)) {
                logger.info("Processing general web search query: {}", query);
                String searchResults = webSearchService.searchWeb(query);
                contextBuilder.append("CURRENT WEB SEARCH RESULTS:\n").append(searchResults).append("\n\n");
            }

            // If we gathered real-time data, enhance the query
            if (contextBuilder.length() > 0) {
                enhancedQuery = String.format("""
                    User query: %s
                    
                    %s
                    
                    Please provide a helpful, conversational response based on the above real-time data. 
                    Keep it concise for voice delivery (2-3 sentences) unless the user asks for details.
                    If using real-time data, briefly mention it's current information.
                    """, query, contextBuilder.toString());
            }

            return generateResponse(enhancedQuery);

        } catch (Exception e) {
            logger.error("Error processing query: {}", e.getMessage());
            return "I encountered an issue processing your request. Please try again.";
        }
    }

    private String generateResponse(String enhancedQuery) {
        try {
            Message userMessage = Message.builder(Message.Role.USER)
                    .content(enhancedQuery)
                    .build();
            conversationHistory.add(userMessage);

            // Trim conversation history if it gets too long
            trimConversationHistory();

            ChatRequest chatRequest = ChatRequest.builder(model)
                    .messages(conversationHistory)
                    .options(OllamaOptions.builder()
                            .temperature(0.7)
                            .build())
                    .build();

            ChatResponse response = ollamaApi.chat(chatRequest);
            String assistantResponse = response.message().content();

            // Add assistant response to history
            Message assistantMessage = Message.builder(Message.Role.ASSISTANT)
                    .content(assistantResponse)
                    .build();
            conversationHistory.add(assistantMessage);

            logger.info("Generated response for query, length: {} characters", assistantResponse.length());
            return assistantResponse;

        } catch (Exception e) {
            logger.error("Error generating AI response: {}", e.getMessage());
            return "I'm having trouble generating a response right now. Please try again.";
        }
    }

    private void trimConversationHistory() {
        // Keep system message and last 8 exchanges (16 messages)
        if (conversationHistory.size() > 17) {
            List<Message> trimmedHistory = new ArrayList<>();
            trimmedHistory.add(conversationHistory.get(0)); // Keep system message
            trimmedHistory.addAll(conversationHistory.subList(
                    conversationHistory.size() - 16,
                    conversationHistory.size()
            ));
            conversationHistory.clear();
            conversationHistory.addAll(trimmedHistory);
        }
    }

    private String extractLocation(String query) {
        var matcher = LOCATION_PATTERN.matcher(query);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null; // Will use default location
    }

    private String extractSport(String query) {
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("football") || lowerQuery.contains("nfl")) return "football";
        if (lowerQuery.contains("basketball") || lowerQuery.contains("nba")) return "basketball";
        if (lowerQuery.contains("baseball") || lowerQuery.contains("mlb")) return "baseball";
        if (lowerQuery.contains("hockey") || lowerQuery.contains("nhl")) return "hockey";
        if (lowerQuery.contains("soccer") || lowerQuery.contains("mls")) return "soccer";
        return "football"; // default
    }

    public void clearConversationHistory() {
        conversationHistory.clear();
        initializeAssistant();
    }

    public boolean hasInternetAccess() {
        return true; // This service now has comprehensive internet access
    }

    public String getCapabilities() {
        return """
            I now have comprehensive internet access capabilities including:
            • Real-time weather information and forecasts
            • Live sports scores and schedules
            • Current news and web search results
            • Up-to-date information on various topics
            
            Just ask me about current weather, sports scores, news, or any topic requiring real-time information!
            """;
    }
}
