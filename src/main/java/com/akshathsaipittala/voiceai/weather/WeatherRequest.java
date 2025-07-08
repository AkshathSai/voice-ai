package com.akshathsaipittala.voiceai.weather;

// mapping the response of the Weather API to records. I only mapped the information I was interested in.
public record WeatherRequest(String city) {
}
