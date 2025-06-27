package com.akshathsaipittala.voiceai.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    // Simple in-memory storage for demo purposes
    private final Map<String, Object> userProfile = new HashMap<>();

    public UserProfileService() {
        // Initialize with default values
        userProfile.put("daily_calorie_goal", 2000);
        userProfile.put("name", "User");
        userProfile.put("preferred_units", "metric");
    }

    public void setDailyCalorieGoal(int calories) {
        userProfile.put("daily_calorie_goal", calories);
        logger.info("Updated daily calorie goal to: {}", calories);
    }

    public int getDailyCalorieGoal() {
        return (Integer) userProfile.getOrDefault("daily_calorie_goal", 2000);
    }

    public void setUserName(String name) {
        userProfile.put("name", name);
        logger.info("Updated user name to: {}", name);
    }

    public String getUserName() {
        return (String) userProfile.getOrDefault("name", "User");
    }

    public void setPreferredUnits(String units) {
        userProfile.put("preferred_units", units);
    }

    public String getPreferredUnits() {
        return (String) userProfile.getOrDefault("preferred_units", "metric");
    }

    public Map<String, Object> getFullProfile() {
        return new HashMap<>(userProfile);
    }
}
