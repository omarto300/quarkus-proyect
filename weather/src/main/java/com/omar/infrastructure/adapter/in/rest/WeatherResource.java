package com.omar.infrastructure.adapter.in.rest;

import com.omar.domain.model.Coordinates;
import com.omar.domain.port.in.GetWeatherUseCase;
import com.omar.infrastructure.adapter.in.rest.dto.WeatherRequestDto;
import com.omar.infrastructure.adapter.in.rest.dto.WeatherResponse;
import com.omar.infrastructure.adapter.in.rest.mapper.WeatherRestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;

@Path("/api/v1/weather")
@Produces(MediaType.APPLICATION_JSON)
@AllArgsConstructor
public class WeatherResource {

  private final GetWeatherUseCase weatherUseCase;
  private final WeatherRestMapper mapper;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<WeatherResponse> getWeather(@Valid WeatherRequestDto request) {
    var coordinates = mapper.toCoordinates(request);
    return weatherUseCase.getWeather(coordinates).map(mapper::toResponse);
  }
}
