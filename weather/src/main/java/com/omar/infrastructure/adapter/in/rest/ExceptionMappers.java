package com.omar.infrastructure.adapter.in.rest;

import com.omar.domain.exception.InvalidCoordinatesException;
import com.omar.domain.exception.WeatherProviderException;
import com.omar.domain.exception.WeatherServiceUnavailableException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @ServerExceptionMapper
  public Response mapInvalidCoordinates(InvalidCoordinatesException e) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(Map.of("error", e.getMessage()))
        .build();
  }

  @ServerExceptionMapper
  public Response mapProviderFailure(WeatherProviderException e) {
    return Response.status(Response.Status.BAD_GATEWAY)
        .entity(Map.of("error", e.getMessage()))
        .build();
  }

  @ServerExceptionMapper
  public Response mapServiceUnavailable(WeatherServiceUnavailableException e) {
    return Response.status(Response.Status.SERVICE_UNAVAILABLE)
        .entity(Map.of("error", e.getMessage()))
        .build();
  }
}
