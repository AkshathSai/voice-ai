package com.akshathsaipittala.voiceai.service;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class NLPService {

    private static final Logger logger = LoggerFactory.getLogger(NLPService.class);
    private TokenizerME tokenizer;

    // Intent keywords mapping
    private final Map<String, List<String>> intentKeywords = new HashMap<>();

    public NLPService() {
        initializeTokenizer();
        initializeIntentKeywords();
    }

    private void initializeTokenizer() {
        try {
            InputStream modelIn = getClass().getResourceAsStream("/models/opennlp/en-token.bin");
            if (modelIn != null) {
                TokenizerModel model = new TokenizerModel(modelIn);
                tokenizer = new TokenizerME(model);
                logger.info("OpenNLP tokenizer initialized successfully");
            } else {
                logger.warn("OpenNLP tokenizer model not found, using simple tokenization");
            }
        } catch (Exception e) {
            logger.error("Error initializing OpenNLP tokenizer: {}", e.getMessage());
        }
    }

    private void initializeIntentKeywords() {
        intentKeywords.put("reminder", Arrays.asList("remind", "reminder", "schedule", "appointment", "calendar"));
        intentKeywords.put("weather", Arrays.asList("weather", "temperature", "rain", "sunny", "cloudy", "forecast"));
        intentKeywords.put("news", Arrays.asList("news", "headlines", "latest", "current events", "breaking"));
        intentKeywords.put("nutrition", Arrays.asList("nutrition", "food", "calories", "ate", "eating", "meal", "diet"));
        intentKeywords.put("code-assistance", Arrays.asList("code", "programming", "debug", "refactor", "function", "python", "java"));
        intentKeywords.put("timer", Arrays.asList("timer", "stopwatch", "countdown", "alarm"));
        intentKeywords.put("system", Arrays.asList("stop", "shutdown", "quit", "status", "help"));
    }

    public String detectIntent(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "unknown";
        }

        String lowerQuery = query.toLowerCase();

        for (Map.Entry<String, List<String>> entry : intentKeywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerQuery.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return "general";
    }

    public Map<String, List<String>> extractEntities(String query) {
        Map<String, List<String>> entities = new HashMap<>();

        if (query == null || query.trim().isEmpty()) {
            return entities;
        }

        // Extract time entities
        List<String> timeEntities = extractTimeEntities(query);
        if (!timeEntities.isEmpty()) {
            entities.put("time", timeEntities);
        }

        // Extract number entities
        List<String> numberEntities = extractNumberEntities(query);
        if (!numberEntities.isEmpty()) {
            entities.put("number", numberEntities);
        }

        // Extract food entities (simple keyword matching)
        List<String> foodEntities = extractFoodEntities(query);
        if (!foodEntities.isEmpty()) {
            entities.put("food", foodEntities);
        }

        return entities;
    }

    private List<String> extractTimeEntities(String query) {
        List<String> timeEntities = new ArrayList<>();

        // Simple time patterns
        Pattern timePattern = Pattern.compile("\\b(\\d{1,2}(?::\\d{2})?(?:\\s*(?:am|pm))?)|(?:in\\s+\\d+\\s+(?:minute|hour)s?)\\b", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = timePattern.matcher(query);

        while (matcher.find()) {
            timeEntities.add(matcher.group().trim());
        }

        return timeEntities;
    }

    private List<String> extractNumberEntities(String query) {
        List<String> numberEntities = new ArrayList<>();

        Pattern numberPattern = Pattern.compile("\\b\\d+\\b");
        java.util.regex.Matcher matcher = numberPattern.matcher(query);

        while (matcher.find()) {
            numberEntities.add(matcher.group());
        }

        return numberEntities;
    }

    private List<String> extractFoodEntities(String query) {
        List<String> foodEntities = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        // Common food keywords
        String[] foodKeywords = {"apple", "banana", "sandwich", "pizza", "chicken", "rice", "pasta",
                                "salad", "burger", "coffee", "tea", "milk", "bread", "eggs"};

        for (String food : foodKeywords) {
            if (lowerQuery.contains(food)) {
                foodEntities.add(food);
            }
        }

        return foodEntities;
    }

    public String[] tokenize(String text) {
        if (tokenizer != null) {
            return tokenizer.tokenize(text);
        } else {
            // Fallback to simple tokenization
            return text.trim().split("\\s+");
        }
    }
}
