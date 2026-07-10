package com.omar.infrastructure.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.omar.WireMockResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = WireMockResource.class, restrictToAnnotatedClass = true)
public class PostResourceTest {

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

  void pruebaFallaPorFaltaPost() {
    given()
        .when()
        .get("/post/v1/posts/9999")
        .then()
        .statusCode(404)
        .body("error", containsString("9999"));
  }
}
