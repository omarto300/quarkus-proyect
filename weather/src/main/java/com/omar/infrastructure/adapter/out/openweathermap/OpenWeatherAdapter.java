package com.omar.infrastructure.adapter.out.openweathermap;

import com.omar.domain.exception.WeatherProviderException;
import com.omar.domain.exception.WeatherServiceUnavailableException;
import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.domain.port.out.WeatherProviderPort;
import com.omar.infrastructure.adapter.out.openweathermap.mapper.OpenWeatherMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class OpenWeatherAdapter implements WeatherProviderPort {

  @Inject @RestClient OpenWeatherClient openWeatherClient;

  @Inject OpenWeatherMapper openWeatherMapper;

  @ConfigProperty(name = "openweathermap.api-key")
  String apiKey;

  @ConfigProperty(name = "openweathermap.units")
  String units;

  @Override
  @Retry(maxRetries = 2, abortOn = CircuitBreakerOpenException.class)
  @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 5000)
  @Timeout(3000)
  @Fallback(fallbackMethod = "fetchWeatherFallback", applyOn = CircuitBreakerOpenException.class)
  public Uni<Weather> fetchWeather(Coordinates coordinates) {
    return openWeatherClient
        .getWeather(coordinates.lat(), coordinates.lon(), apiKey, units)
        .map(openWeatherMapper::toDomain)
        .onFailure()
        .transform(
            e -> new WeatherProviderException("Error consultando OpenWeatherMap", e));
  }

  Uni<Weather> fetchWeatherFallback(Coordinates coordinates) {
    return Uni.createFrom()
        .failure(
            new WeatherServiceUnavailableException(
                "El servicio de clima no está disponible temporalmente, intenta de nuevo más tarde"));
  }
}
