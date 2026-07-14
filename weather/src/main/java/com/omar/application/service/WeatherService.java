package com.omar.application.service;

import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.domain.port.in.GetWeatherUseCase;
import com.omar.domain.port.out.WeatherProviderPort;
import com.omar.infrastructure.adapter.in.rest.dto.WeatherRequestDto;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class WeatherService implements GetWeatherUseCase {

  private final WeatherProviderPort weatherProvider;

  @Override
  public Uni<Weather> getWeather(Coordinates coordinates) {
    return weatherProvider.fetchWeather(coordinates);
  }
}
