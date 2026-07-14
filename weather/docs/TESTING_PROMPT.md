# PROMPT: Boundary Testing for Hexagonal + Clean Architecture (Quarkus)

> Copy/paste this prompt when asking for tests. It defines the rules, stack, and structure the tests MUST follow.

---

## Context & Stack

You are testing a **Quarkus 3.x + Java 21** microservice built with **Hexagonal Architecture (Ports & Adapters) + Clean Architecture**. The stack is:

- **Reactive**: SmallRye Mutiny (`Uni` / `Multi`) — never block with `.await().indefinitely()` in tests; use `UniAssertSubscriber` (official SmallRye Mutiny testing API).
- **Lombok**: constructor injection via `@RequiredArgsConstructor` where applicable.
- **MapStruct**: mappers with `componentModel = "cdi"`.
- **Functional style**: prefer `Optional`, method references, pure functions, immutability (records).
- **Language**: ALL code, test names, and Javadoc in **English**. Only end-user-facing messages (HTTP error bodies) may be in **Spanish**.
- **WireMock**: external HTTP providers are stubbed with mapping + response files under `src/test/resources/__files/`.
- **JSONAssert** (`org.skyscreamer:jsonassert`): full JSON payload comparison at boundaries.
- **RestAssured**: HTTP boundary testing.

---

## Golden Rules (never break these)

1. **Test at boundaries, not internals.** A test enters through a public boundary (constructor of a value object, a use case port, or an HTTP endpoint) and asserts at the exit boundary. Never test private methods or internal wiring.
2. **Mock ONLY external providers** (outbound ports to third-party APIs, databases you don't control). Everything inside the hexagon runs REAL.
3. **One responsibility per test class.** Domain tests know nothing about Quarkus. Boundary tests know nothing about domain internals.
4. **Expected JSON lives in files, not strings.** Response payloads for stubs and expected results go in `src/test/resources/__files/` with the naming convention:
    - `success_<resource>.json` → happy path
    - `error_<resource>_<case>.json` → error scenarios
    - Example: `success_weather.json`, `error_weather_not_found.json`, `error_weather_invalid_key.json`
5. **Assert reactively** with `UniAssertSubscriber`; assert JSON with `JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT)` (or `LENIENT` when field order/extra fields are acceptable).

---

## Test Pyramid (4 levels)

```
Level 4  ── E2E BOUNDARY TEST (HTTP → domain → HTTP stub)   ← the most valuable
Level 3  ── OUTBOUND ADAPTER TEST (adapter → WireMock)
Level 2  ── USE CASE TEST (port in → mocked port out)
Level 1  ── DOMAIN TEST (pure JUnit, invariants)
```

---

## Level 1 — Domain / Value Object Tests (pure JUnit)

**Location:** `src/test/java/.../domain/model/`
**No Quarkus. No mocks. No Mutiny.** The boundary is the constructor / factory.

```java
/**
 * Unit tests for the {@link Coordinates} value object.
 *
 * <p>Verifies that the value object enforces its geographic invariants
 * at construction time (rich domain model, no invalid state possible).
 */
class CoordinatesTest {

  @Test
  void shouldAcceptBoundaryValues() {
    assertDoesNotThrow(() -> new Coordinates(90, 180));
    assertDoesNotThrow(() -> new Coordinates(-90, -180));
  }

  @Test
  void shouldRejectLatitudeAboveUpperBound() {
    assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(90.1, 0));
  }

  @Test
  void shouldRejectLongitudeBelowLowerBound() {
    assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(0, -180.1));
  }
}
```

Notes:
- Test the exact boundary values (`90`, `-90`, `180`, `-180`) AND just past them (`90.1`, `-180.1`).
- Class-level visibility: package-private (`class`, not `public class`) — JUnit 5 convention.

---

## Level 2 — Use Case Tests (@QuarkusTest, mock outbound ports only)

**Location:** `src/test/java/.../application/`
**Boundary in:** inbound port (`GetWeatherUseCase`). **Boundary out:** outbound port (`WeatherProviderPort`, mocked).
The use case implementation, domain models and internal mappers run REAL.

```java
/**
 * Use case boundary test for {@link GetWeatherUseCase}.
 *
 * <p>Enters through the inbound port and mocks only the outbound
 * provider port. All domain logic executes for real.
 */
@QuarkusTest
class GetWeatherUseCaseTest {

  @InjectMock
  WeatherProviderPort weatherProviderPort;

  @Inject
  GetWeatherUseCase getWeather;

  @Test
  void shouldEmitWeatherFromProvider() {
    var request = new WeatherRequestDto(4.61, -74.08);
    var weather = new Weather("Bogota", "clear sky", 20.0, 19.5, 60, 3.0);

    when(weatherProviderPort.fetchWeather(new Coordinates(4.61, -74.08)))
        .thenReturn(Uni.createFrom().item(weather));

    getWeather.getWeather(request)
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .assertItem(weather);
  }

  @Test
  void shouldPropagateProviderFailure() {
    var request = new WeatherRequestDto(4.61, -74.08);

    when(weatherProviderPort.fetchWeather(any()))
        .thenReturn(Uni.createFrom()
            .failure(new WeatherProviderException("external failure", null)));

    getWeather.getWeather(request)
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitFailure()
        .assertFailedWith(WeatherProviderException.class, "external failure");
  }
}
```

Notes:
- `UniAssertSubscriber` is the official SmallRye Mutiny testing API — use it instead of blocking.
- Stub the port with the exact `Coordinates` the use case is expected to build (this also verifies the use case constructs the domain object correctly). Use `any()` only in failure tests where the argument is irrelevant.

---

## Level 3 — Outbound Adapter Tests (WireMock + `__files`)

**Location:** `src/test/java/.../infrastructure/adapter/out/`
**Boundary in:** the outbound port implementation. **Boundary out:** WireMock stub (the ONLY thing simulated — and it's a real HTTP server, not a mock object).

**File layout:**

```
src/test/resources/
├── __files/
│   ├── success_weather.json          # real OpenWeatherMap payload (happy path)
│   ├── error_weather_not_found.json  # 404 body
│   └── error_weather_invalid_key.json
└── application.properties            # test profile pointing the client to WireMock
```

`success_weather.json` (copy a REAL provider response — never invent fields):

```json
{
  "weather": [{ "description": "clear sky" }],
  "main": { "temp": 20.0, "feels_like": 19.5, "humidity": 60 },
  "wind": { "speed": 3.0 },
  "name": "Bogota"
}
```

```java
/**
 * Outbound adapter boundary test for {@link OpenWeatherMapAdapter}.
 *
 * <p>Stubs the external OpenWeatherMap API with WireMock using response
 * bodies from {@code __files}. Verifies HTTP contract, deserialization,
 * MapStruct mapping and error translation — end to end inside the adapter.
 */
@QuarkusTest
@ConnectWireMock
class OpenWeatherMapAdapterTest {

  WireMock wiremock;

  @Inject
  WeatherProviderPort adapter; // test the PORT, not the concrete class

  @Test
  void shouldMapProviderResponseToDomain() {
    wiremock.register(get(urlPathEqualTo("/data/2.5/weather"))
        .withQueryParam("lat", equalTo("4.61"))
        .withQueryParam("lon", equalTo("-74.08"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBodyFile("success_weather.json"))); // ← reads from __files/

    adapter.fetchWeather(new Coordinates(4.61, -74.08))
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .assertItem(new Weather("Bogota", "clear sky", 20.0, 19.5, 60, 3.0));
  }

  @Test
  void shouldTranslateServerErrorToDomainException() {
    wiremock.register(get(urlPathEqualTo("/data/2.5/weather"))
        .willReturn(aResponse()
            .withStatus(500)
            .withBodyFile("error_weather_server.json")));

    adapter.fetchWeather(new Coordinates(4.61, -74.08))
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitFailure()
        .assertFailedWith(WeatherProviderException.class);
  }
}
```

Notes:
- `withBodyFile("x.json")` resolves relative to `__files/` — that is the WireMock convention.
- Inject the **port interface**, not the adapter class: you test the contract, and if you swap adapters the test survives.
- This level verifies: REST client config, JSON deserialization into DTOs, MapStruct DTO→domain mapping, fault-tolerance error translation. All REAL.

---

## Level 4 — E2E Boundary Test (HTTP in → WireMock out) ⭐

**Location:** `src/test/java/.../e2e/` (or `infrastructure/adapter/in/rest/`)
**Boundary in:** real HTTP request (RestAssured). **Boundary out:** WireMock stub of the external provider.
**NOTHING inside the hexagon is mocked.** This is the point-to-point test.

```
HTTP request ──▶ REST resource ──▶ use case ──▶ domain ──▶ adapter ──▶ WireMock
     ▲                                                                    │
     └────────────────── JSON response asserted with JSONAssert ◀────────┘
```

```java
/**
 * End-to-end boundary test: real HTTP request through the full hexagon,
 * with only the external weather provider stubbed via WireMock.
 *
 * <p>Response payloads are asserted with JSONAssert against expected
 * JSON files, guaranteeing the public API contract.
 */
@QuarkusTest
@ConnectWireMock
class WeatherEndToEndTest {

  WireMock wiremock;

  @Test
  void shouldReturnWeatherJsonOnValidRequest() throws Exception {
    wiremock.register(get(urlPathEqualTo("/data/2.5/weather"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBodyFile("success_weather.json")));

    var actual = given()
        .queryParam("lat", 4.61)
        .queryParam("lon", -74.08)
        .when()
        .get("/api/v1/weather")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .extract().asString();

    var expected = readExpected("expected_success_weather_response.json");
    JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
  }

  @Test
  void shouldReturn400WithSpanishMessageOnInvalidLatitude() throws Exception {
    var actual = given()
        .queryParam("lat", 95)
        .queryParam("lon", -74.08)
        .when()
        .get("/api/v1/weather")
        .then()
        .statusCode(400)
        .extract().asString();

    // User-facing message in Spanish is part of the contract
    var expected = """
        {"error": "Latitud fuera de rango (-90..90): 95.0"}
        """;
    JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
  }

  @Test
  void shouldReturn502WhenProviderFails() throws Exception {
    wiremock.register(get(urlPathEqualTo("/data/2.5/weather"))
        .willReturn(aResponse().withStatus(500)));

    given()
        .queryParam("lat", 4.61)
        .queryParam("lon", -74.08)
        .when()
        .get("/api/v1/weather")
        .then()
        .statusCode(502);
  }

  private String readExpected(String fileName) throws IOException {
    try (var is = getClass().getResourceAsStream("/__files/" + fileName)) {
      return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
```

**Expected-response files also live in `__files/`:**

```
__files/
├── success_weather.json                     # provider stub body (input)
├── expected_success_weather_response.json   # MY API's expected output
└── error_weather_server.json
```

`expected_success_weather_response.json`:

```json
{
  "city": "Bogota",
  "description": "clear sky",
  "temperature": 20.0,
  "feelsLike": 19.5,
  "humidity": 60,
  "windSpeed": 3.0
}
```

---

## Required test dependencies

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-junit5</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-junit5-mockito</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>rest-assured</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>io.quarkiverse.wiremock</groupId>
  <artifactId>quarkus-wiremock-test</artifactId>
  <version>1.6.3</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.skyscreamer</groupId>
  <artifactId>jsonassert</artifactId>
  <version>1.5.3</version>
  <scope>test</scope>
</dependency>
```

---

## Decision table

| I want to test... | Level | Annotations | Simulated | Assertion tool |
|---|---|---|---|---|
| Domain invariants | 1 | none (pure JUnit) | nothing | `assertThrows` / `assertThat` |
| Use case orchestration | 2 | `@QuarkusTest` | outbound ports (`@InjectMock`) | `UniAssertSubscriber` |
| External API adapter | 3 | `@QuarkusTest @ConnectWireMock` | provider HTTP (`__files`) | `UniAssertSubscriber` |
| Full public contract | 4 | `@QuarkusTest @ConnectWireMock` | provider HTTP only | RestAssured + `JSONAssert` |

---

## Naming conventions

- Test classes: `<Subject>Test` (levels 1–3), `<Feature>EndToEndTest` (level 4).
- Test methods: `should<Outcome>[When<Condition>]` — always English.
    - `shouldEmitWeatherFromProvider`
    - `shouldRejectLatitudeAboveUpperBound`
    - `shouldReturn400WithSpanishMessageOnInvalidLatitude`
- JSON files: `success_*`, `error_*_<case>`, `expected_*` inside `__files/`.

## Anti-patterns to reject

- ❌ Mocking domain objects, use cases (except in isolated REST-mapping tests), or internal mappers.
- ❌ `.await().indefinitely()` in tests — use `UniAssertSubscriber`.
- ❌ Inline JSON strings for provider payloads — use `withBodyFile` + `__files`.
- ❌ Field-by-field assertions on JSON contracts — use `JSONAssert`.
- ❌ Spanish identifiers/method names — Spanish only in user-facing message content.
- ❌ Testing the concrete adapter class when the port interface can be injected.