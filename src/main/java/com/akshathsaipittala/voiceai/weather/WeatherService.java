package com.akshathsaipittala.voiceai.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

/*
   Weather API
   https://www.weatherapi.com/api-explorer.aspx
 */
public class WeatherService implements Function<WeatherRequest, WeatherService.Response> {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private final RestClient restClient;
    private final WeatherConfigProperties props;

    public WeatherService(WeatherConfigProperties props) {
        this.props = props;
        log.info("Weather API URL: {}", props.apiUrl());
        log.info("Weather API Key: {}", props.apiKey());
        this.restClient = RestClient.create(props.apiUrl());
    }

    public Response apply(WeatherRequest weatherRequest) {
        log.info("Weather WeatherRequest: {}", weatherRequest);
        Response response = restClient.get()
                .uri("/current.json?key={key}&q={q}", props.apiKey(), weatherRequest.city())
                .retrieve()
                .body(Response.class);
        log.info("Weather API Response: {}", response);
        return response;
    }


    public record Response(Location location, Current current) {
    }

    public record Location(String name, String region, String country, Long lat, Long lon) {
    }

    public record Current(String temp_f, Condition condition, String wind_mph, String humidity) {
    }

    public record Condition(String text) {
    }

}