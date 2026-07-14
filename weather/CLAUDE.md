# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Quarkus (3.37.2, Java 21) REST service, groupId `com.omar`, artifactId `weather`. Expone `GET /api/v1/weather?lat=&lon=`, que consulta la API de OpenWeatherMap y devuelve el clima actual normalizado en JSON. Implementado con arquitectura hexagonal (domain / application / infrastructure).

Ver [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) para el detalle de capas, puertos y el recorrido completo de una petición.

## Commands

Use the Maven wrapper (`./mvnw`), not a system `mvn`.

- Dev mode (live reload): `./mvnw quarkus:dev`
- Run all tests: `./mvnw test`
- Run a single test: `./mvnw test -Dtest=WeatherResourceTest`
- Run a single test method: `./mvnw test -Dtest=WeatherResourceTest#devuelveElClimaMapeado`
- Package: `./mvnw package` → runnable via `java -jar target/quarkus-app/quarkus-run.jar`
- Über-jar: `./mvnw package -Dquarkus.package.jar.type=uber-jar`
- Native executable: `./mvnw package -Dnative` (or `-Dquarkus.native.container-build=true` without GraalVM installed)
- Quarkus Dev UI (dev mode only): http://localhost:8080/q/dev/

## Architecture

Arquitectura hexagonal bajo `src/main/java/com/omar/`:

- `domain/model` — `Coordinates` (valida lat ∈ [-90,90] / lon ∈ [-180,180]) y `Weather`, ambos records inmutables sin dependencias externas.
- `domain/exception` — `InvalidCoordinatesException`, `WeatherProviderException`.
- `domain/port/in` — `GetWeatherUseCase` (puerto de entrada, caso de uso).
- `domain/port/out` — `WeatherProviderPort` (puerto de salida hacia cualquier proveedor de clima).
- `application/service` — `WeatherService`, implementa `GetWeatherUseCase` delegando en `WeatherProviderPort`.
- `infrastructure/adapter/in/rest` — `WeatherResource` (`GET /api/v1/weather`), `ExceptionMappers` (mapea excepciones de dominio a HTTP: 400/502), `WeatherRestMapper` (MapStruct) y el DTO `WeatherResponse`.
- `infrastructure/adapter/out/openweathermap` — `OpenWeatherClient` (REST client MicroProfile), `OpenWeatherAdapter` (implementa `WeatherProviderPort`, con `@Retry`/`@Timeout` de `quarkus-smallrye-fault-tolerance`), DTOs `Owm*Dto` y `OpenWeatherMapper` (MapStruct).

Regla de dependencia: `infrastructure` depende de `domain` (vía los puertos), `domain` no depende de nada. `application` conecta puerto de entrada con puerto de salida.

### Configuración

- `src/main/resources/application.yml` (no hay `application.properties`; habilitado por la extensión `quarkus-config-yaml`).
- Variable de entorno requerida: `OPENWEATHER_API_KEY` (ver `.env.example`). En dev/test se puede definir en un `.env` local (gitignorado).
- Extensiones relevantes además de `quarkus-arc`/`quarkus-rest`: `quarkus-rest-client-jackson` (cliente hacia OpenWeatherMap), `quarkus-smallrye-fault-tolerance` (retry/timeout), `quarkus-config-yaml`. **No** están presentes `quarkus-smallrye-openapi` ni `quarkus-smallrye-health` — no hay `/q/openapi`, `/q/swagger-ui` ni `/q/health`.

### Tests

- `src/test/java/com/omar/` — `@QuarkusTest` classes use REST-assured against the running dev service; the matching `*IT` class (e.g. `WeatherResourceIT`) extends the `@QuarkusTest` class and reruns the same tests via `@QuarkusIntegrationTest` against the packaged artifact. Add new endpoint tests following this same-class-extended-by-IT pattern.
- Integration tests (`*IT`) only run during `./mvnw verify`/`failsafe`, not plain `./mvnw test`; the `skipITs` property gates them and is flipped on by the `native` Maven profile.
- `OpenWeatherMapWireMockResource` (`@QuarkusTestResource`) levanta un WireMock en puerto dinámico y sobreescribe `quarkus.rest-client.openweathermap.url`/`openweathermap.api-key` durante los tests de `WeatherResourceTest`/`WeatherResourceIT`, para no depender de la API real de OpenWeatherMap.

### Docker

- `src/main/docker/` — Dockerfiles for jvm, legacy-jar, native, and native-micro packaging modes (not built automatically by Maven).
