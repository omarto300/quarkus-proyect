# weather

Servicio Quarkus que expone el clima actual de una ubicación (`lat`/`lon`), consultando la API de [OpenWeatherMap](https://openweathermap.org/api). Implementado con arquitectura hexagonal (domain / application / infrastructure) — ver [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) para el detalle de capas, puertos y el recorrido completo de una petición.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/weather?lat={lat}&lon={lon}` | Clima actual para la coordenada dada. |

### `GET /api/v1/weather`

Parámetros de consulta (ambos obligatorios):

- `lat` — latitud, entre `-90` y `90`.
- `lon` — longitud, entre `-180` y `180`.

Respuesta `200 OK`:

```json
{
  "ciudad": "Ciudad de México",
  "descripcion": "cielo claro",
  "temperatura": 22.5,
  "sensacionTermica": 21.8,
  "humedad": 40,
  "velocidadViento": 3.6
}
```

Errores:

| Código | Causa |
|---|---|
| `400 Bad Request` | Falta `lat`/`lon`, o están fuera de rango. Cuerpo: `{"error": "<mensaje>"}`. |
| `502 Bad Gateway` | OpenWeatherMap no respondió correctamente o dentro del tiempo esperado. Cuerpo: `{"error": "<mensaje>"}`. |
| `503 Service Unavailable` | El circuit breaker está abierto (OpenWeatherMap falló repetidamente hace poco) y el servicio se protege sin llamarlo. Cuerpo: `{"error": "<mensaje>"}`. Ver [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md#resiliencia). |

## Configuración

La configuración vive en `src/main/resources/application.yml` (YAML, habilitado por la extensión `quarkus-config-yaml`). La única variable de entorno requerida es:

- `OPENWEATHER_API_KEY` — API key de OpenWeatherMap.

Para desarrollo local, copia la plantilla y completa tu key:

```shell script
cp .env.example .env
```

Quarkus dev mode carga automáticamente el `.env` de la raíz del proyecto.

## Correr en modo desarrollo (live reload)

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus ships with a Dev UI, available in dev mode only at <http://localhost:8080/q/dev/>.

## Tests

```shell script
# Unitarios + tests REST (contra el servicio en modo dev, con OpenWeatherMap simulado vía WireMock)
./mvnw test

# Además de lo anterior, corre las pruebas *IT contra el artefacto empaquetado
./mvnw verify
```

- `./mvnw test` corre los `@QuarkusTest` (incluyendo `WeatherResourceTest`, que levanta un WireMock local en vez de llamar a la API real de OpenWeatherMap).
- Las clases `*IT` (p. ej. `WeatherResourceIT`) sólo corren con `./mvnw verify`/`failsafe` — reejecutan los mismos casos vía `@QuarkusIntegrationTest` contra el jar empaquetado.

## Empaquetado y ejecución

```shell script
./mvnw package
```

Genera `quarkus-run.jar` en `target/quarkus-app/` (no es über-jar; las dependencias quedan en `target/quarkus-app/lib/`).

```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

Para generar un über-jar:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

```shell script
java -jar target/*-runner.jar
```

## Ejecutable nativo

```shell script
./mvnw package -Dnative
```

O, sin GraalVM instalado, compilando en un contenedor:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Ejecutar con: `./target/weather-1.0.0-SNAPSHOT-runner`

Más info: <https://quarkus.io/guides/maven-tooling>.

## Docker

Dockerfiles listos en `src/main/docker/` (jvm, legacy-jar, native, native-micro). No se construyen automáticamente por Maven; ver el encabezado de cada archivo para el comando `docker build` correspondiente.
