package com.omar.infrastructure.adapter.in.rest;

import com.omar.domain.exception.InvalidCoordinatesException;
import com.omar.domain.exception.WeatherProviderException;
import com.omar.domain.exception.WeatherServiceUnavailableException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class WeatherExceptionMapper {

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

  @ServerExceptionMapper
  public Response mapConstraintViolation(ConstraintViolationException e) {
    String message = e.getConstraintViolations().stream()
        .findFirst()
        .map(cv -> {
          String path = cv.getPropertyPath().toString();
          int dot = path.lastIndexOf('.');
          String param = dot >= 0 ? path.substring(dot + 1) : path;
          return param + " " + cv.getMessage();
        })
        .orElse("Invalid request parameters");
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(Map.of("error", message))
        .build();
  }
}
