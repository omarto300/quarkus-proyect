package com.omar.infrastructure.adapter.out.openweathermap;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

public class OpenWeatherMapWireMockResource implements QuarkusTestResourceLifecycleManager {

  private static WireMockServer server;

  public static WireMockServer getServer() {
    return server;
  }

  @Override
  public Map<String, String> start() {
    server = new WireMockServer(options().dynamicPort());
    server.start();
    return Map.of(
        "quarkus.rest-client.openweathermap.url", server.baseUrl(),
        "openweathermap.api-key", "test-key");
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
