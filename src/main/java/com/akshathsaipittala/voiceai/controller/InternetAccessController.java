package com.akshathsaipittala.voiceai.controller;

import com.akshathsaipittala.voiceai.service.AIService;
import com.akshathsaipittala.voiceai.service.WeatherService;
import com.akshathsaipittala.voiceai.service.SportsService;
import com.akshathsaipittala.voiceai.service.WebSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internet")
public class InternetAccessController {

    private final AIService aiService;
    private final WeatherService weatherService;
    private final SportsService sportsService;
    private final WebSearchService webSearchService;

    @Autowired
    public InternetAccessController(AIService aiService, WeatherService weatherService,
                                  SportsService sportsService, WebSearchService webSearchService) {
        this.aiService = aiService;
        this.weatherService = weatherService;
        this.sportsService = sportsService;
        this.webSearchService = webSearchService;
    }

    @GetMapping("/capabilities")
    public Map<String, Object> getCapabilities() {
        return Map.of(
            "internetAccess", true,
            "services", Map.of(
                "weather", "Current weather and forecasts",
                "sports", "Live scores and schedules",
                "webSearch", "General web search and news",
                "realTimeData", "Up-to-date information"
            ),
            "exampleQueries", java.util.List.of(
                "What's the weather like in New York?",
                "Latest NFL scores",
                "Current news about AI",
                "Today's basketball games"
            )
        );
    }

    @PostMapping("/weather")
    public Map<String, Object> getWeather(@RequestBody Map<String, String> request) {
        String location = request.getOrDefault("location", "");
        String result = weatherService.getCurrentWeather(location);

        return Map.of(
            "service", "weather",
            "location", location.isEmpty() ? "default" : location,
            "result", result,
            "timestamp", java.time.Instant.now()
        );
    }

    @PostMapping("/sports")
    public Map<String, Object> getSports(@RequestBody Map<String, String> request) {
        String sport = request.getOrDefault("sport", "football");
        String type = request.getOrDefault("type", "scores"); // "scores" or "schedule"

        String result = type.equals("schedule")
            ? sportsService.getTodaysGames(sport)
            : sportsService.getSportsScores(sport, null);

        return Map.of(
            "service", "sports",
            "sport", sport,
            "type", type,
            "result", result,
            "timestamp", java.time.Instant.now()
        );
    }

    @PostMapping("/search")
    public Map<String, Object> webSearch(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return Map.of("error", "Query cannot be empty");
        }

        boolean requiresSearch = webSearchService.requiresInternetSearch(query);
        String result = requiresSearch
            ? webSearchService.searchWeb(query)
            : "This query doesn't require internet search.";

        return Map.of(
            "service", "webSearch",
            "query", query,
            "requiresInternet", requiresSearch,
            "result", result,
            "timestamp", java.time.Instant.now()
        );
    }

    @PostMapping("/ask")
    public Map<String, Object> askWithInternetAccess(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Map.of("error", "Question cannot be empty");
        }

        // Use the enhanced AI service with internet access
        String response = aiService.processQuery(question);

        return Map.of(
            "question", question,
            "response", response,
            "internetEnabled", true,
            "timestamp", java.time.Instant.now()
        );
    }

    @GetMapping("/test")
    public Map<String, Object> testInternetAccess() {
        return Map.of(
            "status", "Internet access is configured",
            "message", "Your voice AI can now access real-time information!",
            "testQueries", java.util.List.of(
                "Try: 'What's the weather today?'",
                "Try: 'Latest sports scores'",
                "Try: 'Current news about technology'"
            ),
            "setupInstructions", "Add API keys to application.properties for full functionality"
        );
    }
}
