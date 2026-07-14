# weather

Servicio Quarkus que expone el clima actual de una ubicación (`lat`/`lon`), consultando la API de [OpenWeatherMap](https://openweathermap.org/api). Implementado con arquitectura hexagonal (domain / application / infrastructure) — ver [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) para el detalle de capas, puertos y el recorrido completo de una petición.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/weather` | Clima actual para la coordenada dada (en el cuerpo de la petición). |

### `POST /api/v1/weather`

Cuerpo de la petición (JSON, `Content-Type: application/json`), ambos campos obligatorios:

- `latitude` — latitud, entre `-90` y `90`.
- `longitude` — longitud, entre `-180` y `180`.

```json
{
  "latitude": 19.43,
  "longitude": -99.13
}
```

```shell script
curl -X POST http://localhost:8080/api/v1/weather \
  -H "Content-Type: application/json" \
  -d '{"latitude": 19.43, "longitude": -99.13}'
```

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
| `400 Bad Request` | Falta `latitude`/`longitude`, o están fuera de rango. Cuerpo: `{"error": "<mensaje>"}`. |
| `502 Bad Gateway` | OpenWeatherMap no respondió correctamente o dentro del tiempo esperado. Cuerpo: `{"error": "<mensaje>"}`. |
| `503 Service Unavailable` | El circuit breaker está abierto (OpenWeatherMap falló repetidamente hace poco) y el servicio se protege sin llamarlo. Cuerpo: `{"error": "<mensaje>"}`. Ver [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md#resiliencia). |
