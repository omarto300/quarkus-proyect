package com.omar.application.service;

import static org.mockito.Mockito.when;

import com.omar.domain.exception.WeatherProviderException;
import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.domain.port.in.GetWeatherUseCase;
import com.omar.domain.port.out.WeatherProviderPort;
import com.omar.infrastructure.adapter.in.rest.dto.WeatherRequestDto;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GetWeatherUseCaseTest {

  @InjectMock WeatherProviderPort weatherProviderPort;
  @Inject GetWeatherUseCase getWeather;

  @Test
  void shouldEmitWeatherFromProvider() {
    var coordinates = new Coordinates(4.61, -74.08);
    var weather = new Weather("Bogota", "clear sky", 20.0, 19.5, 60, 3.0);
    when(weatherProviderPort.fetchWeather(coordinates)).thenReturn(Uni.createFrom().item(weather));

    getWeather.getWeather(coordinates)
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .assertItem(weather);
  }

  @Test
  void shouldPropagateProviderFailure() {
    var coordinates = new Coordinates(4.61, -74.08);
    when(weatherProviderPort.fetchWeather(coordinates))
        .thenReturn(Uni.createFrom().failure(new WeatherProviderException("external failure", null)));

    getWeather.getWeather(coordinates)
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitFailure()
        .assertFailedWith(WeatherProviderException.class, "external failure");
  }
}
