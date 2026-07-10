package com.omar.infrastructure.adapter.out.openweathermap;

import com.omar.infrastructure.adapter.out.openweathermap.dto.OwmResponseDto;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "openweathermap")
public interface OpenWeatherClient {

  @GET
  @Path("/data/2.5/weather")
  Uni<OwmResponseDto> getWeather(
      @QueryParam("lat") double lat,
      @QueryParam("lon") double lon,
      @QueryParam("appid") String appid,
      @QueryParam("units") String units);
}
