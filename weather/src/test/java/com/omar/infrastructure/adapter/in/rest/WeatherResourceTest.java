package com.omar.infrastructure.adapter.in.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import com.omar.infrastructure.adapter.out.openweathermap.OpenWeatherMapWireMockResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@QuarkusTestResource(OpenWeatherMapWireMockResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WeatherResourceTest {

  private static final String OWM_RESPONSE =
      """
      {
        "name": "London",
        "weather": [{"description": "light rain"}],
        "main": {"temp": 15.5, "feels_like": 14.2, "humidity": 82},
        "wind": {"speed": 4.1}
      }
      """;

  @BeforeEach
  void resetStubs() {
    OpenWeatherMapWireMockResource.getServer().resetAll();
  }

  @Test
  @Order(1)
  void devuelveElClimaMapeado() {
    OpenWeatherMapWireMockResource.getServer()
        .stubFor(
            get(urlPathEqualTo("/data/2.5/weather"))
                .willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBody(OWM_RESPONSE)));

    given()
        .queryParam("lat", 51.51)
        .queryParam("lon", -0.13)
        .when()
        .get("/api/v1/weather")
        .then()
        .statusCode(200)
        .body("ciudad", is("London"))
        .body("descripcion", is("light rain"))
        .body("humedad", is(82));
  }

  @Test
  @Order(2)
  void coordenadasInvalidasDevuelven400() {
    given()
        .queryParam("lat", 200)
        .queryParam("lon", 0)
        .when()
        .get("/api/v1/weather")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(3)
  void falloDelProveedorDevuelve502() {
    OpenWeatherMapWireMockResource.getServer()
        .stubFor(get(urlPathEqualTo("/data/2.5/weather")).willReturn(aResponse().withStatus(500)));

    given()
        .queryParam("lat", 10.0)
        .queryParam("lon", 10.0)
        .when()
        .get("/api/v1/weather")
        .then()
        .statusCode(502);
  }

  @Test
  @Order(4)
  void circuitoAbiertoDevuelve503YDejaDeLlamarAlProveedor() {
    OpenWeatherMapWireMockResource.getServer()
        .stubFor(get(urlPathEqualTo("/data/2.5/weather")).willReturn(aResponse().withStatus(500)));

    int status = 0;
    for (int i = 0; i < 5 && status != 503; i++) {
      status =
          given()
              .queryParam("lat", 20.0)
              .queryParam("lon", 20.0)
              .when()
              .get("/api/v1/weather")
              .then()
              .extract()
              .statusCode();
    }

    org.junit.jupiter.api.Assertions.assertEquals(503, status);

    int requestsBeforeFastFail =
        OpenWeatherMapWireMockResource.getServer()
            .countRequestsMatching(getRequestedFor(urlPathEqualTo("/data/2.5/weather")).build())
            .getCount();

    given()
        .queryParam("lat", 20.0)
        .queryParam("lon", 20.0)
        .when()
        .get("/api/v1/weather")
        .then()
        .statusCode(503);

    int requestsAfterFastFail =
        OpenWeatherMapWireMockResource.getServer()
            .countRequestsMatching(getRequestedFor(urlPathEqualTo("/data/2.5/weather")).build())
            .getCount();

    org.junit.jupiter.api.Assertions.assertEquals(requestsBeforeFastFail, requestsAfterFastFail);
  }
}
