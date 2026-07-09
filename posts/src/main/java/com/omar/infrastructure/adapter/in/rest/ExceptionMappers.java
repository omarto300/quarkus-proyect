package com.omar.infrastructure.adapter.in.rest;

import com.omar.domain.exception.PostNotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @ServerExceptionMapper
  public Response mapNotFound(PostNotFoundException e) {
    return Response.status(Response.Status.NOT_FOUND)
        .entity(Map.of("error", e.getMessage()))
        .build();
  }
}
