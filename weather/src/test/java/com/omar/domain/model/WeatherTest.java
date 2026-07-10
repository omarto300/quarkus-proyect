package com.omar.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WeatherTest {

  @Test
  void temperaturaModeradaNoEsExtrema() {
    var weather = new Weather("Bogota", "clear sky", 20.0, 19.5, 60, 3.0);
    assertFalse(weather.isExtremeTemperature());
  }

  @Test
  void calorExtremoEsExtremo() {
    var weather = new Weather("Death Valley", "clear sky", 35.0, 38.0, 10, 1.0);
    assertTrue(weather.isExtremeTemperature());
  }

  @Test
  void frioExtremoEsExtremo() {
    var weather = new Weather("Oymyakon", "snow", -10.0, -18.0, 80, 2.0);
    assertTrue(weather.isExtremeTemperature());
  }
}
