package com.akshathsaipittala.voiceai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SportsService {

    private static final Logger logger = LoggerFactory.getLogger(SportsService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sports.api.key:}")
    private String sportsApiKey;

    public SportsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getSportsScores(String sport, String league) {
        if (sportsApiKey.isEmpty()) {
            return "Sports service is not configured. Please add your sports API key to application.properties.";
        }

        try {
            // Using ESPN API as an example (free tier available)
            String url = buildSportsApiUrl(sport, league);
            String response = restTemplate.getForObject(url, String.class);
            return parseSportsResponse(response, sport, league);

        } catch (Exception e) {
            logger.error("Error fetching sports data: {}", e.getMessage());
            return "I'm unable to fetch sports information right now. Please try again later.";
        }
    }

    public String getTodaysGames(String sport) {
        try {
            // Get today's date
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            if (sportsApiKey.isEmpty()) {
                return getFallbackSportsInfo(sport, today);
            }

            String url = buildTodaysGamesUrl(sport, today);
            String response = restTemplate.getForObject(url, String.class);
            return parseTodaysGamesResponse(response, sport);

        } catch (Exception e) {
            logger.error("Error fetching today's games: {}", e.getMessage());
            return "I'm unable to fetch today's games right now. Please try again later.";
        }
    }

    private String buildSportsApiUrl(String sport, String league) {
        // This is a generic structure - you'd need to adapt based on your chosen sports API
        // Example for ESPN API or similar
        String sportCode = mapSportToCode(sport);
        String leagueCode = mapLeagueToCode(league);

        return String.format(
            "https://api.sportsdata.io/v3/%s/scores/json/GamesByDate/%s?key=%s",
            sportCode, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), sportsApiKey
        );
    }

    private String buildTodaysGamesUrl(String sport, String date) {
        String sportCode = mapSportToCode(sport);
        return String.format(
            "https://api.sportsdata.io/v3/%s/scores/json/GamesByDate/%s?key=%s",
            sportCode, date, sportsApiKey
        );
    }

    private String parseSportsResponse(String response, String sport, String league) {
        try {
            JsonNode root = objectMapper.readTree(response);
            StringBuilder result = new StringBuilder();

            if (root.isArray() && root.size() > 0) {
                result.append(String.format("Recent %s scores:\n\n", sport));

                int count = 0;
                for (JsonNode game : root) {
                    if (count >= 5) break; // Limit to 5 games

                    String homeTeam = game.get("HomeTeam").asText();
                    String awayTeam = game.get("AwayTeam").asText();
                    int homeScore = game.get("HomeScore").asInt();
                    int awayScore = game.get("AwayScore").asInt();
                    String status = game.get("Status").asText();

                    result.append(String.format(
                        "%s vs %s: %d - %d (%s)\n",
                        awayTeam, homeTeam, awayScore, homeScore, status
                    ));
                    count++;
                }
            } else {
                result.append(String.format("No recent %s games found.", sport));
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("Error parsing sports response: {}", e.getMessage());
            return "Unable to parse sports data.";
        }
    }

    private String parseTodaysGamesResponse(String response, String sport) {
        try {
            JsonNode root = objectMapper.readTree(response);
            StringBuilder result = new StringBuilder();

            if (root.isArray() && root.size() > 0) {
                result.append(String.format("Today's %s games:\n\n", sport));

                for (JsonNode game : root) {
                    String homeTeam = game.get("HomeTeam").asText();
                    String awayTeam = game.get("AwayTeam").asText();
                    String dateTime = game.get("DateTime").asText();
                    String status = game.get("Status").asText();

                    result.append(String.format(
                        "%s @ %s - %s (%s)\n",
                        awayTeam, homeTeam, dateTime, status
                    ));
                }
            } else {
                result.append(String.format("No %s games scheduled for today.", sport));
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("Error parsing today's games response: {}", e.getMessage());
            return "Unable to parse today's games data.";
        }
    }

    private String mapSportToCode(String sport) {
        if (sport == null) return "nfl";

        String lowerSport = sport.toLowerCase();
        return switch (lowerSport) {
            case "football", "nfl" -> "nfl";
            case "basketball", "nba" -> "nba";
            case "baseball", "mlb" -> "mlb";
            case "hockey", "nhl" -> "nhl";
            case "soccer", "mls" -> "mls";
            default -> "nfl";
        };
    }

    private String mapLeagueToCode(String league) {
        if (league == null) return "";

        String lowerLeague = league.toLowerCase();
        return switch (lowerLeague) {
            case "nfl", "national football league" -> "nfl";
            case "nba", "national basketball association" -> "nba";
            case "mlb", "major league baseball" -> "mlb";
            case "nhl", "national hockey league" -> "nhl";
            case "mls", "major league soccer" -> "mls";
            default -> league.toLowerCase();
        };
    }

    private String getFallbackSportsInfo(String sport, String date) {
        return String.format(
            "Sports service is not fully configured. To get real-time %s scores and schedules for %s, " +
            "please add your sports API key to application.properties. " +
            "You can use services like SportsData.io or ESPN API.",
            sport, date
        );
    }

    public boolean isSportsQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("sports") || lowerQuery.contains("score") ||
               lowerQuery.contains("game") || lowerQuery.contains("match") ||
               lowerQuery.contains("football") || lowerQuery.contains("basketball") ||
               lowerQuery.contains("baseball") || lowerQuery.contains("hockey") ||
               lowerQuery.contains("soccer") || lowerQuery.contains("nfl") ||
               lowerQuery.contains("nba") || lowerQuery.contains("mlb") ||
               lowerQuery.contains("nhl") || lowerQuery.contains("mls");
    }
}
