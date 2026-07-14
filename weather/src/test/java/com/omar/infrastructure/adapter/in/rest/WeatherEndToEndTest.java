package com.omar.infrastructure.adapter.in.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@QuarkusTest
@ConnectWireMock
class WeatherEndToEndTest {

  private static final double LAT = 51.51;
  private static final double LON = -0.13;

  WireMock wiremock;

  @Inject CircuitBreakerMaintenance circuitBreakerMaintenance;

  @BeforeEach
  void setUp() {
    wiremock.resetMappings();
    circuitBreakerMaintenance.resetAll();
  }

  @Test
  void shouldReturnMappedWeatherOnValidRequest() throws Exception {
    wiremock.register(
        get(urlPathEqualTo("/data/2.5/weather"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("success_weather.json")));

    String actual =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody(LAT, LON))
            .when()
            .post("/api/v1/weather")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    JSONAssert.assertEquals(readExpected("expected_success_weather_response.json"), actual, JSONCompareMode.STRICT);
  }

  @Test
  void shouldReturn400OnInvalidCoordinates() throws Exception {
    String actual =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody(200, 0))
            .when()
            .post("/api/v1/weather")
            .then()
            .statusCode(400)
            .extract()
            .body()
            .asString();

    JSONAssert.assertEquals(
        "{\"error\": \"Latitude out of range (-90..90): 200.0\"}", actual, JSONCompareMode.STRICT);
  }

  @Test
  void shouldReturn400WhenLatParamIsMissing() throws Exception {
    String actual =
        given()
            .contentType(ContentType.JSON)
            .body("{\"longitude\": " + LON + "}")
            .when()
            .post("/api/v1/weather")
            .then()
            .statusCode(400)
            .extract()
            .body()
            .asString();

    JSONAssert.assertEquals("{\"error\": \"latitude must not be null\"}", actual, JSONCompareMode.STRICT);
  }

  @Test
  void shouldReturn502WhenProviderFails() throws Exception {
    wiremock.register(
        get(urlPathEqualTo("/data/2.5/weather"))
            .willReturn(aResponse().withStatus(500)));

    String actual =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody(LAT, LON))
            .when()
            .post("/api/v1/weather")
            .then()
            .statusCode(502)
            .extract()
            .body()
            .asString();

    JSONAssert.assertEquals(
        "{\"error\": \"Error querying OpenWeatherMap\"}", actual, JSONCompareMode.STRICT);
  }

  @Test
  void shouldReturn503AndStopCallingProviderWhenCircuitIsOpen() throws Exception {
    wiremock.register(
        get(urlPathEqualTo("/data/2.5/weather"))
            .willReturn(aResponse().withStatus(500)));

    int status = 0;
    for (int i = 0; i < 5 && status != 503; i++) {
      status =
          given()
              .contentType(ContentType.JSON)
              .body(requestBody(20.0, 20.0))
              .when()
              .post("/api/v1/weather")
              .then()
              .extract()
              .statusCode();
    }

    Assertions.assertEquals(503, status, "Circuit breaker should have opened");

    String actual =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody(20.0, 20.0))
            .when()
            .post("/api/v1/weather")
            .then()
            .statusCode(503)
            .extract()
            .body()
            .asString();

    JSONAssert.assertEquals(
        "{\"error\": \"Weather service is temporarily unavailable, please try again later\"}",
        actual,
        JSONCompareMode.STRICT);
  }

  private static String requestBody(double lat, double lon) {
    return "{\"latitude\":" + lat + ",\"longitude\":" + lon + "}";
  }

  private String readExpected(String fileName) throws IOException {
    try (var is = getClass().getResourceAsStream("/__files/" + fileName)) {
      return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
