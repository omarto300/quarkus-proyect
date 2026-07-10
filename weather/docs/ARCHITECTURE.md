# Arquitectura

Este servicio implementa el endpoint `GET /api/v1/weather` siguiendo **arquitectura hexagonal** (puertos y adaptadores). El objetivo es mantener la lógica de dominio (qué es una coordenada válida, qué es el clima) totalmente independiente de detalles técnicos como REST o el proveedor externo de clima (hoy OpenWeatherMap, mañana podría ser otro).

## Capas

```
                 ┌─────────────────────────────────────────────┐
                 │              infrastructure                  │
                 │                                               │
   HTTP request  │  adapter/in/rest          adapter/out/        │  HTTP request
  ──────────────▶│  WeatherResource          openweathermap      │─────────────▶ OpenWeatherMap API
                 │  ExceptionMappers         OpenWeatherClient    │
                 │  WeatherRestMapper        OpenWeatherAdapter   │
                 │  WeatherResponse (DTO)    OpenWeatherMapper    │
                 │        │                        ▲              │
                 └────────┼────────────────────────┼──────────────┘
                          │ implementa             │ implementa
                          ▼                        │
                 ┌─────────────────────────────────┴───────────┐
                 │                application                   │
                 │  WeatherService (GetWeatherUseCase)          │
                 └────────────────────┬──────────────────────────┘
                                       │ usa
                                       ▼
                 ┌───────────────────────────────────────────────┐
                 │                   domain                        │
                 │  model:      Coordinates, Weather               │
                 │  exception:  InvalidCoordinatesException,       │
                 │              WeatherProviderException,          │
                 │              WeatherServiceUnavailableException │
                 │  port/in:    GetWeatherUseCase                  │
                 │  port/out:   WeatherProviderPort                │
                 └───────────────────────────────────────────────┘
```

**Regla de dependencia:** las flechas de dependencia de código siempre apuntan hacia adentro. `domain` no importa nada de `application` ni de `infrastructure`. `application` sólo conoce los puertos del `domain`. `infrastructure` es la única capa que conoce frameworks (JAX-RS, MicroProfile REST Client, Jackson, MapStruct, Lombok).

### `domain`

- `model/Coordinates` — record `(lat, lon)`; su constructor compacto valida los rangos (`lat` ∈ [-90,90], `lon` ∈ [-180,180]) y lanza `InvalidCoordinatesException` si no se cumplen. Es imposible construir un `Coordinates` inválido.
- `model/Weather` — record con los datos normalizados del clima (`city`, `description`, `temperature`, `feelsLike`, `humidity`, `windSpeed`) más el helper `isExtremeTemperature()`.
- `port/in/GetWeatherUseCase` — contrato del caso de uso: `Uni<Weather> getWeather(Coordinates coordinates)`.
- `port/out/WeatherProviderPort` — contrato que debe cumplir cualquier proveedor externo de clima: `Uni<Weather> fetchWeather(Coordinates coordinates)`.

### `application`

- `service/WeatherService` — único caso de uso implementado hoy. Implementa `GetWeatherUseCase` e inyecta (constructor) un `WeatherProviderPort`; simplemente delega. Es el punto donde, si el dominio creciera, iría lógica de orquestación adicional (cacheo, combinación de fuentes, etc.).

### `infrastructure`

Adaptadores concretos, en dos direcciones:

- **Entrada (`adapter/in/rest`)** — expone el caso de uso vía HTTP:
  - `WeatherResource` (`@Path("/api/v1/weather")`) lee `lat`/`lon` como query params, construye `Coordinates` y llama a `GetWeatherUseCase`.
  - `WeatherRestMapper` (MapStruct) convierte `Weather` (dominio) → `WeatherResponse` (DTO de salida).
  - `ExceptionMappers` traduce excepciones de dominio a respuestas HTTP (ver más abajo).
  - `WeatherResponse` mantiene sus campos Java en inglés (mismos nombres que `Weather`, para que MapStruct siga matcheando por nombre) pero expone cada uno con `@JsonProperty` en español (`ciudad`, `descripcion`, `temperatura`, `sensacionTermica`, `humedad`, `velocidadViento`) — es decir, el contrato JSON hacia el cliente está en español aunque el modelo de dominio y los datos que llegan de OpenWeatherMap permanecen en inglés.

- **Salida (`adapter/out/openweathermap`)** — implementa el puerto `WeatherProviderPort` contra un proveedor real:
  - `OpenWeatherClient` — interfaz de MicroProfile REST Client (`@RegisterRestClient(configKey = "openweathermap")`) que llama `GET /data/2.5/weather`.
  - `OpenWeatherAdapter` — implementa `WeatherProviderPort`; inyecta `OpenWeatherClient` y `OpenWeatherMapper`; combina `@Retry`, `@CircuitBreaker`, `@Timeout` y `@Fallback` (ver sección "Resiliencia"); envuelve cualquier fallo genuino del proveedor en `WeatherProviderException`.
  - `OpenWeatherMapper` (MapStruct) — convierte el DTO externo `OwmResponseDto` al modelo de dominio `Weather`.
  - DTOs `Owm*Dto` — reflejan literalmente el JSON de la API de OpenWeatherMap (no se reutilizan como modelo de dominio).

## Recorrido de una petición

`GET /api/v1/weather?lat=19.43&lon=-99.13`

1. `WeatherResource` recibe `lat`/`lon`. Si falta alguno, lanza `InvalidCoordinatesException("lat es requerido"` / `"lon es requerido")`.
2. Construye `new Coordinates(lat, lon)` — si están fuera de rango, el propio record lanza `InvalidCoordinatesException`.
3. Llama a `WeatherService.getWeather(coordinates)` (puerto de entrada).
4. `WeatherService` delega en `WeatherProviderPort.fetchWeather(coordinates)`, implementado por `OpenWeatherAdapter`.
5. `OpenWeatherAdapter` llama a `OpenWeatherClient` protegido por `@Retry`/`@CircuitBreaker`/`@Timeout`; si OpenWeatherMap falla o no responde a tiempo, se lanza `WeatherProviderException`. Si el circuito ya está abierto (fallos recientes acumulados), el `@Fallback` responde de inmediato con `WeatherServiceUnavailableException`, sin llamar a OpenWeatherMap.
6. La respuesta exitosa (`OwmResponseDto`) se mapea a `Weather` vía `OpenWeatherMapper`.
7. De vuelta en `WeatherResource`, `Weather` se mapea a `WeatherResponse` vía `WeatherRestMapper` y se serializa como JSON (200 OK).

### Manejo de errores

`ExceptionMappers` (`@ServerExceptionMapper`) centraliza la traducción excepción de dominio → HTTP:

| Excepción de dominio | HTTP | Cuerpo |
|---|---|---|
| `InvalidCoordinatesException` | 400 Bad Request | `{"error": "<mensaje>"}` |
| `WeatherProviderException` | 502 Bad Gateway | `{"error": "<mensaje>"}` |
| `WeatherServiceUnavailableException` | 503 Service Unavailable | `{"error": "<mensaje>"}` |

## Resiliencia

`OpenWeatherAdapter.fetchWeather` combina cuatro anotaciones de MicroProfile Fault Tolerance (`quarkus-smallrye-fault-tolerance`):

```java
@Retry(maxRetries = 2, abortOn = CircuitBreakerOpenException.class)
@CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 5000)
@Timeout(3000)
@Fallback(fallbackMethod = "fetchWeatherFallback", applyOn = CircuitBreakerOpenException.class)
```

- **`@Retry`** — ante un fallo, reintenta hasta 2 veces más.
- **`@CircuitBreaker`** — si de una ventana de al menos 4 ejecuciones, el 50% o más falla, el circuito se abre por 5 segundos: durante ese tiempo, las llamadas fallan de inmediato sin golpear la red real, protegiendo a OpenWeatherMap (y a nuestro propio servicio) de seguir insistiendo sobre un proveedor caído.
- **`@Timeout`** — corta la llamada si tarda más de 3s, contando como fallo.
- **`@Fallback`** — captura específicamente `CircuitBreakerOpenException` (ver `applyOn`) y responde con `WeatherServiceUnavailableException` en vez de dejar escapar la excepción interna de fault tolerance.

Dos detalles de configuración evitan comportamientos indeseados al combinar estas anotaciones:

- `abortOn = CircuitBreakerOpenException.class` en `@Retry`: sin esto, cada llamada con el circuito abierto gastaría 2 reintentos inútiles sobre un fallo que ya sabemos local (no llega a la red, pero no aporta nada). Con `abortOn`, el fallo de circuito abierto pasa directo al fallback.
- `applyOn = CircuitBreakerOpenException.class` en `@Fallback`: sin esto, el fallback capturaría **cualquier** fallo — incluyendo los `WeatherProviderException` genuinos ya construidos por `.onFailure().transform(...)` dentro del propio método — y los convertiría todos en 503, ocultando errores reales del proveedor. Con `applyOn`, solo el circuito abierto dispara el fallback; un fallo genuino de OpenWeatherMap sigue devolviendo 502 vía `ExceptionMappers`.

En resumen: **502** significa "OpenWeatherMap respondió mal o no respondió a tiempo" (error real, tras agotar los reintentos). **503** significa "nos estamos protegiendo: OpenWeatherMap ha fallado tanto que dejamos de llamarlo por un momento" (el circuito está abierto).

## Convenciones usadas

- **DTOs** (`WeatherResponse`, `Owm*Dto`) usan Lombok (`@Data`, `@Value`, `@Builder`, `@NoArgsConstructor`) para reducir boilerplate; nunca se usan como modelo de dominio.
- **Mapeo** entre capas se hace con MapStruct (`componentModel = "cdi"`), generando beans inyectables por CDI en vez de mapeo manual.
- **Modelo de dominio** (`Coordinates`, `Weather`) son `record` inmutables — el estado inválido no es representable.
- **Tests**: unitarios puros para las reglas de dominio (`CoordinatesTest`, `WeatherTest`), `@QuarkusTest` con `@InjectMock` para la capa de aplicación (`WeatherServiceTest`), y `@QuarkusTest` + WireMock (`OpenWeatherMapWireMockResource`) para probar `WeatherResource` de punta a punta sin depender de la API real. Cada clase de test REST tiene su par `*IT` que reejecuta los mismos casos contra el artefacto empaquetado (`@QuarkusIntegrationTest`).
