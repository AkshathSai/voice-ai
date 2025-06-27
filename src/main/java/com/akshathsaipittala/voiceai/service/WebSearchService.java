package com.akshathsaipittala.voiceai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class WebSearchService {

    private static final Logger logger = LoggerFactory.getLogger(WebSearchService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${web.search.serpapi.key:}")
    private String serpApiKey;

    @Value("${web.search.newsapi.key:}")
    private String newsApiKey;

    // Keywords that typically require internet search
    private static final Set<String> SEARCH_KEYWORDS = Set.of(
        "weather", "temperature", "forecast", "rain", "snow", "climate",
        "news", "latest", "current", "today", "breaking", "update",
        "sports", "score", "game", "match", "tournament", "league",
        "stock", "price", "market", "trading", "crypto", "bitcoin",
        "events", "happening", "schedule", "calendar",
        "traffic", "route", "directions",
        "restaurant", "open", "hours", "nearby"
    );

    private static final Pattern CURRENT_TIME_PATTERN = Pattern.compile(
        "\\b(what|current|today|now|latest|recent|this)\\b.*\\b(time|date|weather|news|score|price|update)\\b",
        Pattern.CASE_INSENSITIVE
    );

    public WebSearchService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public boolean requiresInternetSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String lowerQuery = query.toLowerCase();

        // Check for explicit current/real-time indicators
        if (CURRENT_TIME_PATTERN.matcher(query).find()) {
            return true;
        }

        // Check for specific keywords
        return SEARCH_KEYWORDS.stream().anyMatch(lowerQuery::contains);
    }

    public String searchWeb(String query) {
        try {
            logger.info("Performing web search for: {}", query);

            // Try SerpAPI first (most comprehensive)
            if (!serpApiKey.isEmpty()) {
                String result = searchWithSerpAPI(query);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }

            // Fallback to NewsAPI for news-related queries
            if (!newsApiKey.isEmpty() && isNewsQuery(query)) {
                String result = searchWithNewsAPI(query);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }

            // If no API keys are configured, return a message
            return generateFallbackResponse(query);

        } catch (Exception e) {
            logger.error("Error performing web search: {}", e.getMessage());
            return "I'm unable to fetch current information right now. Please try again later.";
        }
    }

    private String searchWithSerpAPI(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                "https://serpapi.com/search.json?q=%s&api_key=%s&num=5",
                encodedQuery, serpApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            return parseSerpAPIResponse(response);

        } catch (HttpClientErrorException e) {
            logger.warn("SerpAPI request failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error with SerpAPI search: {}", e.getMessage());
            return null;
        }
    }

    private String searchWithNewsAPI(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                "https://newsapi.org/v2/everything?q=%s&apiKey=%s&sortBy=publishedAt&pageSize=5",
                encodedQuery, newsApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            return parseNewsAPIResponse(response);

        } catch (HttpClientErrorException e) {
            logger.warn("NewsAPI request failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error with NewsAPI search: {}", e.getMessage());
            return null;
        }
    }

    private String parseSerpAPIResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            StringBuilder result = new StringBuilder();

            // Parse organic results
            JsonNode organicResults = root.get("organic_results");
            if (organicResults != null && organicResults.isArray()) {
                int count = 0;
                for (JsonNode item : organicResults) {
                    if (count >= 3) break; // Limit to top 3 results

                    String title = item.get("title").asText();
                    String snippet = item.get("snippet").asText();
                    String link = item.get("link").asText();

                    result.append(String.format("**%s**\n%s\nSource: %s\n\n",
                        title, snippet, link));
                    count++;
                }
            }

            // Parse answer box if available
            JsonNode answerBox = root.get("answer_box");
            if (answerBox != null) {
                String answer = answerBox.get("answer").asText("");
                if (!answer.isEmpty()) {
                    result.insert(0, String.format("**Direct Answer:** %s\n\n", answer));
                }
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("Error parsing SerpAPI response: {}", e.getMessage());
            return null;
        }
    }

    private String parseNewsAPIResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            StringBuilder result = new StringBuilder();

            JsonNode articles = root.get("articles");
            if (articles != null && articles.isArray()) {
                result.append("**Latest News:**\n\n");
                int count = 0;
                for (JsonNode article : articles) {
                    if (count >= 3) break; // Limit to top 3 articles

                    String title = article.get("title").asText();
                    String description = article.get("description").asText("");
                    String source = article.get("source").get("name").asText();
                    String publishedAt = article.get("publishedAt").asText();

                    result.append(String.format("**%s**\n%s\nSource: %s | Published: %s\n\n",
                        title, description, source, publishedAt));
                    count++;
                }
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("Error parsing NewsAPI response: {}", e.getMessage());
            return null;
        }
    }

    private boolean isNewsQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("news") || lowerQuery.contains("breaking") ||
               lowerQuery.contains("latest") || lowerQuery.contains("headlines");
    }

    private String generateFallbackResponse(String query) {
        return String.format(
            "I don't currently have access to real-time information for '%s'. " +
            "To enable internet search capabilities, please configure API keys in your application properties. " +
            "You can use SerpAPI for general web search or NewsAPI for news-related queries.",
            query
        );
    }
}
