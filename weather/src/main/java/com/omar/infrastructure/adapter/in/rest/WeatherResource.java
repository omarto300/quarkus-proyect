package com.omar.infrastructure.adapter.in.rest;

import com.omar.domain.exception.InvalidCoordinatesException;
import com.omar.domain.model.Coordinates;
import com.omar.domain.port.in.GetWeatherUseCase;
import com.omar.infrastructure.adapter.in.rest.dto.WeatherResponse;
import com.omar.infrastructure.adapter.in.rest.mapper.WeatherRestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/api/v1/weather")
@Produces(MediaType.APPLICATION_JSON)
public class WeatherResource {

  private final GetWeatherUseCase weatherUseCase;
  private final WeatherRestMapper mapper;

  public WeatherResource(GetWeatherUseCase weatherUseCase, WeatherRestMapper mapper) {
    this.weatherUseCase = weatherUseCase;
    this.mapper = mapper;
  }

  @GET
  public Uni<WeatherResponse> getWeather(@QueryParam("lat") Double lat, @QueryParam("lon") Double lon) {
    var coordinates =
        new Coordinates(
            Optional.ofNullable(lat)
                .orElseThrow(() -> new InvalidCoordinatesException("lat es requerido")),
            Optional.ofNullable(lon)
                .orElseThrow(() -> new InvalidCoordinatesException("lon es requerido")));
    return weatherUseCase.getWeather(coordinates).map(mapper::toResponse);
  }
}
