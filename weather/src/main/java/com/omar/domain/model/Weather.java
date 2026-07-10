package com.omar.domain.model;

public record Weather(
    String city, String description, double temperature, double feelsLike, int humidity, double windSpeed) {
  public boolean isExtremeTemperature() {
    return temperature <= -10 || temperature >= 35;
  }
}
