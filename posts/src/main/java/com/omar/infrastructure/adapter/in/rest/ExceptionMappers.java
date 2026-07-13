package com.omar.infrastructure.adapter.in.rest;

import com.omar.domain.exception.PostNotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.Map;

import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @ServerExceptionMapper
  public Response mapNotFound(PostNotFoundException e) {
    return Response.status(Response.Status.NOT_FOUND)
        .entity(Map.of("error", e.getMessage()))
        .build();
  }

  @ServerExceptionMapper(TimeoutException.class)
  public Response timeOutException() {
    return Response.status(Response.Status.GATEWAY_TIMEOUT)
        .entity(Map.of("error", "Tiempo de espera excedido"))
        .build();
  }
}
