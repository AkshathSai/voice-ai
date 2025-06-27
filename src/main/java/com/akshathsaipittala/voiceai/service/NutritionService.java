package com.akshathsaipittala.voiceai.service;

import com.akshathsaipittala.voiceai.model.NutritionEntry;
import com.akshathsaipittala.voiceai.repository.NutritionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutritionService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionService.class);
    private final NutritionRepository nutritionRepository;
    private final UserProfileService userProfileService;

    // Local food database for calorie estimation
    private final Map<String, Integer> foodCalorieDatabase = new HashMap<>();

    @Autowired
    public NutritionService(NutritionRepository nutritionRepository, UserProfileService userProfileService) {
        this.nutritionRepository = nutritionRepository;
        this.userProfileService = userProfileService;
        initializeFoodDatabase();
    }

    private void initializeFoodDatabase() {
        // Basic food calorie database for local estimation
        foodCalorieDatabase.put("apple", 95);
        foodCalorieDatabase.put("banana", 105);
        foodCalorieDatabase.put("orange", 62);
        foodCalorieDatabase.put("sandwich", 300);
        foodCalorieDatabase.put("salad", 150);
        foodCalorieDatabase.put("chicken breast", 231);
        foodCalorieDatabase.put("rice", 130);
        foodCalorieDatabase.put("pasta", 220);
        foodCalorieDatabase.put("pizza", 285);
        foodCalorieDatabase.put("burger", 540);
        foodCalorieDatabase.put("yogurt", 100);
        foodCalorieDatabase.put("eggs", 78);
        foodCalorieDatabase.put("bread", 79);
        foodCalorieDatabase.put("oatmeal", 158);
        foodCalorieDatabase.put("almonds", 162);
        foodCalorieDatabase.put("cheese", 113);
        foodCalorieDatabase.put("milk", 42);
        foodCalorieDatabase.put("coffee", 2);
        foodCalorieDatabase.put("tea", 2);
        // Add more foods as needed
    }

    public NutritionEntry logFood(String foodDescription, String mealType, Integer calories) {
        NutritionEntry entry = new NutritionEntry(foodDescription, mealType, calories);
        NutritionEntry saved = nutritionRepository.save(entry);
        logger.info("Logged food: {} ({} calories) for {}", foodDescription, calories, mealType);
        return saved;
    }

    public int estimateCalories(String foodDescription) {
        String lowerFood = foodDescription.toLowerCase();

        // Check for exact matches first
        for (Map.Entry<String, Integer> entry : foodCalorieDatabase.entrySet()) {
            if (lowerFood.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default estimation based on food type keywords
        if (lowerFood.contains("salad")) return 150;
        if (lowerFood.contains("soup")) return 200;
        if (lowerFood.contains("sandwich") || lowerFood.contains("wrap")) return 350;
        if (lowerFood.contains("pizza")) return 285;
        if (lowerFood.contains("burger")) return 540;
        if (lowerFood.contains("pasta")) return 220;
        if (lowerFood.contains("rice")) return 130;
        if (lowerFood.contains("chicken")) return 231;
        if (lowerFood.contains("fish") || lowerFood.contains("salmon")) return 206;
        if (lowerFood.contains("fruit")) return 80;
        if (lowerFood.contains("vegetable")) return 25;
        if (lowerFood.contains("snack") || lowerFood.contains("chip")) return 150;
        if (lowerFood.contains("drink") || lowerFood.contains("soda")) return 140;

        // Default estimation
        return 200;
    }

    public List<NutritionEntry> getTodaysEntries() {
        return nutritionRepository.findByEntryDate(LocalDate.now());
    }

    public int getTodaysTotalCalories() {
        List<NutritionEntry> todaysEntries = getTodaysEntries();
        return todaysEntries.stream()
                .mapToInt(entry -> entry.getCalories() != null ? entry.getCalories() : 0)
                .sum();
    }

    public List<NutritionEntry> getEntriesForDateRange(LocalDate startDate, LocalDate endDate) {
        return nutritionRepository.findByEntryDateBetween(startDate, endDate);
    }

    public void setDailyCalorieGoal(int calories) {
        userProfileService.setDailyCalorieGoal(calories);
        logger.info("Set daily calorie goal to: {}", calories);
    }

    public int getDailyCalorieGoal() {
        return userProfileService.getDailyCalorieGoal();
    }

    public Map<String, Integer> getWeeklyCalorieSummary() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<NutritionEntry> weekEntries = getEntriesForDateRange(startDate, endDate);
        Map<String, Integer> dailyTotals = new HashMap<>();

        for (NutritionEntry entry : weekEntries) {
            String dateKey = entry.getEntryDate().toString();
            dailyTotals.merge(dateKey, entry.getCalories() != null ? entry.getCalories() : 0, Integer::sum);
        }

        return dailyTotals;
    }
}
