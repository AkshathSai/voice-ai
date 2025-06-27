package com.akshathsaipittala.voiceai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);
    private final WebClient webClient;

    @Value("${news.api.base-url}")
    private String baseUrl;

    @Value("${news.api.key}")
    private String apiKey;

    public NewsService() {
        this.webClient = WebClient.builder().build();
    }

    public List<String> getLatestHeadlines(String category) {
        try {
            if (apiKey == null || apiKey.equals("YOUR_FREE_NEWS_API_KEY")) {
                return getMockNewsHeadlines(category);
            }

            // For demo purposes, return mock data to avoid API dependency
            return getMockNewsHeadlines(category);

        } catch (Exception e) {
            logger.error("Error fetching news headlines: {}", e.getMessage());
            return Arrays.asList("Sorry, I couldn't retrieve news headlines right now.");
        }
    }

    private List<String> getMockNewsHeadlines(String category) {
        List<String> headlines = new ArrayList<>();

        if (category != null && category.equalsIgnoreCase("technology")) {
            headlines.add("Apple announces new M4 chip with enhanced AI capabilities");
            headlines.add("Microsoft integrates advanced AI features into Office suite");
            headlines.add("Google releases new quantum computing breakthrough");
            headlines.add("Tesla unveils latest autonomous driving technology");
            headlines.add("OpenAI launches improved language model with better reasoning");
        } else if (category != null && category.equalsIgnoreCase("sports")) {
            headlines.add("Local team wins championship in thrilling match");
            headlines.add("Olympic preparations underway for upcoming games");
            headlines.add("Tennis tournament sees unexpected upsets");
            headlines.add("Football season kicks off with record attendance");
            headlines.add("Basketball playoffs reach exciting semifinals");
        } else if (category != null && category.equalsIgnoreCase("business")) {
            headlines.add("Stock market reaches new highs amid economic optimism");
            headlines.add("Major tech companies report strong quarterly earnings");
            headlines.add("New startup receives significant venture capital funding");
            headlines.add("Global supply chain shows signs of stabilization");
            headlines.add("Cryptocurrency market experiences renewed interest");
        } else {
            // General news
            headlines.add("Local community celebrates annual festival");
            headlines.add("New environmental initiative launched by city council");
            headlines.add("Healthcare advances show promising results");
            headlines.add("Education sector adopts innovative teaching methods");
            headlines.add("Transportation infrastructure receives major upgrade");
        }

        return headlines;
    }

    public List<String> getBreakingNews() {
        List<String> breakingNews = Arrays.asList(
            "Major weather system approaching the region",
            "Local government announces new policy changes",
            "Community event scheduled for this weekend"
        );

        return breakingNews;
    }
}
