package com.omar.application.service;

import static org.mockito.Mockito.when;

import com.omar.domain.exception.WeatherProviderException;
import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.domain.port.out.WeatherProviderPort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetWeatherUseCaseTest {

  @Mock WeatherProviderPort weatherProviderPort;
  @InjectMocks WeatherService weatherService;

  @Test
  void shouldEmitWeatherFromProvider() {
    var coordinates = new Coordinates(4.61, -74.08);
    var weather = new Weather("Bogota", "clear sky", 20.0, 19.5, 60, 3.0);
    when(weatherProviderPort.fetchWeather(coordinates)).thenReturn(Uni.createFrom().item(weather));

    weatherService.getWeather(coordinates)
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .assertItem(weather);
  }

  @Test
  void shouldPropagateProviderFailure() {
    var coordinates = new Coordinates(4.61, -74.08);
    when(weatherProviderPort.fetchWeather(coordinates))
        .thenReturn(Uni.createFrom().failure(new WeatherProviderException("external failure", null)));

    weatherService.getWeather(coordinates)
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitFailure()
        .assertFailedWith(WeatherProviderException.class, "external failure");
  }
}
