package com.omar.infrastructure.adapter.out.openweathermap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.omar.domain.exception.WeatherProviderException;
import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.domain.port.out.WeatherProviderPort;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@ConnectWireMock
class OpenWeatherMapAdapterTest {

  WireMock wiremock;

  @Inject WeatherProviderPort adapter;
  @Inject CircuitBreakerMaintenance circuitBreakerMaintenance;

  @BeforeEach
  void setUp() {
    wiremock.resetMappings();
    circuitBreakerMaintenance.resetAll();
  }

  @Test
  void shouldMapProviderResponseToDomain() {
    wiremock.register(
        get(urlPathEqualTo("/data/2.5/weather"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("success_weather.json")));

    adapter.fetchWeather(new Coordinates(51.51, -0.13))
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .assertItem(new Weather("London", "light rain", 15.5, 14.2, 82, 4.1));
  }

  @Test
  void shouldTranslateServerErrorToDomainException() {
    wiremock.register(
        get(urlPathEqualTo("/data/2.5/weather"))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withBodyFile("error_weather_server.json")));

    adapter.fetchWeather(new Coordinates(51.51, -0.13))
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitFailure()
        .assertFailedWith(WeatherProviderException.class);
  }
}
