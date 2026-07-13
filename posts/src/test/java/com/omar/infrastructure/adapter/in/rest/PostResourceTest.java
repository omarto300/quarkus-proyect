package com.omar.infrastructure.adapter.in.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.omar.WireMockResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@QuarkusTest
@QuarkusTestResource(value = WireMockResource.class)
public class PostResourceTest {

  WireMockServer server;

  @BeforeEach
  public void resetWiremock() {
    if (server != null) {
      server.resetRequests();
      server.resetScenarios();
    }
  }
  @Test
  void pruebaIntegracionDetailPost() {
    given()
        .when()
        .get("/api/v1/posts/1")
        .then()
        .statusCode(200)
        .body("title", equalTo("titulo wiremock"))
        .body("author", equalTo("Leane Fake"))
        .body("largePost", is(false));
  }

  @Test
  void pruebaFallaPorFaltaPost() {
    given()
        .when()
        .get("/api/v1/posts/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("9999"));
  }

  @Test
  void timeoutCuandoProvedorLento() {

    given()
        .when()
        .get("/api/v1/posts/2")
        .then()
        .statusCode(504)
        .time(lessThan(4000L), TimeUnit.MILLISECONDS);

  }

  @Test
  void retrySalvaLlamadaIntermitente() {
    given()
    .when()
        .get("/api/v1/posts/3")
        .then()
        .statusCode(200);
    server.verify(2,getRequestedFor(urlEqualTo("/posts/3")));
  }
}
