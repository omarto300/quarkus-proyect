package com.omar;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

public class WireMockResource implements QuarkusTestResourceLifecycleManager {

  private WireMockServer server;

  @Override
  public Map<String, String> start() {
    server = new WireMockServer(0);
    server.start();

    server.stubFor(
        get(urlEqualTo("/posts/1"))
            .willReturn(
                okJson(
                    """
                    {"id": 1, "userId": 9, "title": "titulo wiremock", "body": "cuerpo corto"}
                    """)));
    server.stubFor(
        get(urlEqualTo("/users/9"))
            .willReturn(
                okJson(
                    """
                    {"id": 9, "name": "Leane Fake", "username": "fake", "email": "fake@test.com"}
                    """)));

    server.stubFor(
        get(urlEqualTo("/posts/9999")).willReturn(aResponse().withStatus(404).withBody("{}")));
    return Map.of("quarkus.rest-client.jsonplaceholder.url", server.baseUrl());
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
