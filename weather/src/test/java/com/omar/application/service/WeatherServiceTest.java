package com.omar.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.omar.domain.exception.WeatherProviderException;
import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.domain.port.in.GetWeatherUseCase;
import com.omar.domain.port.out.WeatherProviderPort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WeatherServiceTest {

  @InjectMock WeatherProviderPort weatherProviderPort;
  @Inject GetWeatherUseCase getWeather;

  @Test
  void devuelveElClimaDelProveedor() {
    var coordinates = new Coordinates(4.61, -74.08);
    var weather = new Weather("Bogota", "clear sky", 20.0, 19.5, 60, 3.0);
    when(weatherProviderPort.fetchWeather(coordinates)).thenReturn(Uni.createFrom().item(weather));

    var result = getWeather.getWeather(coordinates).await().atMost(Duration.ofSeconds(2));

    assertEquals("Bogota", result.city());
    assertEquals(20.0, result.temperature());
  }

  @Test
  void propagaFalloDelProveedor() {
    var coordinates = new Coordinates(4.61, -74.08);
    when(weatherProviderPort.fetchWeather(coordinates))
        .thenReturn(Uni.createFrom().failure(new WeatherProviderException("falla externa", null)));

    assertThrows(
        WeatherProviderException.class,
        () -> getWeather.getWeather(coordinates).await().atMost(Duration.ofSeconds(2)));
  }
}
