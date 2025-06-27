package com.akshathsaipittala.voiceai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${weather.openweathermap.key:}")
    private String openWeatherMapKey;

    @Value("${weather.default.city:New York}")
    private String defaultCity;

    public WeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getCurrentWeather(String location) {
        if (openWeatherMapKey.isEmpty()) {
            return "Weather service is not configured. Please add your OpenWeatherMap API key to application.properties.";
        }

        try {
            String city = location != null && !location.trim().isEmpty() ? location : defaultCity;
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);

            String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric",
                encodedCity, openWeatherMapKey
            );

            String response = restTemplate.getForObject(url, String.class);
            return parseWeatherResponse(response, city);

        } catch (Exception e) {
            logger.error("Error fetching weather data: {}", e.getMessage());
            return "I'm unable to fetch weather information right now. Please try again later.";
        }
    }

    public String getWeatherForecast(String location, int days) {
        if (openWeatherMapKey.isEmpty()) {
            return "Weather service is not configured. Please add your OpenWeatherMap API key to application.properties.";
        }

        try {
            String city = location != null && !location.trim().isEmpty() ? location : defaultCity;
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);

            String url = String.format(
                "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&cnt=%d",
                encodedCity, openWeatherMapKey, Math.min(days * 8, 40) // API returns 8 forecasts per day, max 40
            );

            String response = restTemplate.getForObject(url, String.class);
            return parseForecastResponse(response, city, days);

        } catch (Exception e) {
            logger.error("Error fetching weather forecast: {}", e.getMessage());
            return "I'm unable to fetch weather forecast right now. Please try again later.";
        }
    }

    private String parseWeatherResponse(String response, String city) {
        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode main = root.get("main");
            JsonNode weather = root.get("weather").get(0);
            JsonNode wind = root.get("wind");

            double temp = main.get("temp").asDouble();
            double feelsLike = main.get("feels_like").asDouble();
            int humidity = main.get("humidity").asInt();
            String description = weather.get("description").asText();
            double windSpeed = wind.get("speed").asDouble();

            return String.format(
                "Current weather in %s:\n" +
                "Temperature: %.1f°C (feels like %.1f°C)\n" +
                "Condition: %s\n" +
                "Humidity: %d%%\n" +
                "Wind Speed: %.1f m/s",
                city, temp, feelsLike, description, humidity, windSpeed
            );

        } catch (Exception e) {
            logger.error("Error parsing weather response: {}", e.getMessage());
            return "Unable to parse weather data.";
        }
    }

    private String parseForecastResponse(String response, String city, int days) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode list = root.get("list");

            StringBuilder forecast = new StringBuilder();
            forecast.append(String.format("Weather forecast for %s:\n\n", city));

            String currentDate = "";
            int dayCount = 0;

            for (JsonNode item : list) {
                if (dayCount >= days) break;

                String dateTime = item.get("dt_txt").asText();
                String date = dateTime.split(" ")[0];
                String time = dateTime.split(" ")[1];

                // Only show midday forecasts for daily summary
                if (time.equals("12:00:00")) {
                    JsonNode main = item.get("main");
                    JsonNode weather = item.get("weather").get(0);

                    double temp = main.get("temp").asDouble();
                    String description = weather.get("description").asText();

                    forecast.append(String.format(
                        "%s: %.1f°C, %s\n",
                        date, temp, description
                    ));
                    dayCount++;
                }
            }

            return forecast.toString();

        } catch (Exception e) {
            logger.error("Error parsing forecast response: {}", e.getMessage());
            return "Unable to parse forecast data.";
        }
    }

    public boolean isWeatherQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("weather") || lowerQuery.contains("temperature") ||
               lowerQuery.contains("forecast") || lowerQuery.contains("rain") ||
               lowerQuery.contains("snow") || lowerQuery.contains("sunny") ||
               lowerQuery.contains("cloudy") || lowerQuery.contains("humid");
    }
}
