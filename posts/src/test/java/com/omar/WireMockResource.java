package com.omar;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

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

    server.stubFor(get(urlEqualTo("/posts/2")).willReturn(
        okJson("""
            {"id": 2, "userId": 9, "title": "lento", "body": "zzz"}
            """)
            .withFixedDelay(5000)));


    server.stubFor(get(urlEqualTo("/posts/3"))
            .inScenario("Intermitencia")
        .whenScenarioStateIs(STARTED)
            .willSetStateTo("OK")
        .willReturn(serverError()));

    server.stubFor(get(urlEqualTo("/posts/3"))
        .inScenario("Intermitencia")
        .whenScenarioStateIs("OK")
        .willReturn(okJson("""
            {"id": 3, "userId": 9, "title": "Falla una", "body": "zzz"}
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

  @Override
  public void inject(TestInjector testInjector) {
    testInjector.injectIntoFields(
        server,
        new TestInjector.MatchesType(WireMockServer.class)
    );
  }
}
