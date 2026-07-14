package com.omar.infrastructure.adapter.out.openweathermap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import com.omar.domain.exception.WeatherProviderException;
import com.omar.domain.exception.WeatherServiceUnavailableException;
import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.infrastructure.adapter.out.openweathermap.dto.OpenWeatherMapResponseDto;
import com.omar.infrastructure.adapter.out.openweathermap.mapper.OpenWeatherMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenWeatherAdapterUnitTest {

  @Mock OpenWeatherClient openWeatherClient;
  @Mock OpenWeatherMapper openWeatherMapper;

  OpenWeatherAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new OpenWeatherAdapter(openWeatherClient, openWeatherMapper, "test-key", "metric");
  }

  @Test
  void shouldFetchAndMapWeather() {
    var coordinates = new Coordinates(51.51, -0.13);
    var dto = new OpenWeatherMapResponseDto();
    var weather = new Weather("London", "clear", 15.0, 14.0, 80, 3.0);

    when(openWeatherClient.getWeather(51.51, -0.13, "test-key", "metric"))
        .thenReturn(Uni.createFrom().item(dto));
    when(openWeatherMapper.toDomain(dto)).thenReturn(weather);

    adapter.fetchWeather(coordinates)
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .assertItem(weather);
  }

  @Test
  void shouldWrapProviderExceptionOnFailure() {
    var coordinates = new Coordinates(51.51, -0.13);

    when(openWeatherClient.getWeather(anyDouble(), anyDouble(), any(), any()))
        .thenReturn(Uni.createFrom().failure(new RuntimeException("network error")));

    adapter.fetchWeather(coordinates)
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitFailure()
        .assertFailedWith(WeatherProviderException.class, "Error querying OpenWeatherMap");
  }

  @Test
  void shouldReturnFailureFromFallback() {
    adapter.fetchWeatherFallback(new Coordinates(0.0, 0.0))
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitFailure()
        .assertFailedWith(WeatherServiceUnavailableException.class);
  }
}
