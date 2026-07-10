package com.omar.application.service;

import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.domain.port.in.GetWeatherUseCase;
import com.omar.domain.port.out.WeatherProviderPort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WeatherService implements GetWeatherUseCase {
  private final WeatherProviderPort weatherProvider;

  public WeatherService(WeatherProviderPort weatherProvider) {
    this.weatherProvider = weatherProvider;
  }

  @Override
  public Uni<Weather> getWeather(Coordinates coordinates) {
    return weatherProvider.fetchWeather(coordinates);
  }
}
