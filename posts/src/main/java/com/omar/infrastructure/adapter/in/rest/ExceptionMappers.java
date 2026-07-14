package com.omar.infrastructure.adapter.in.rest;

import com.omar.domain.exception.PostNotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 *  Maneja excepciones de los mappers.
 *
 */
public class ExceptionMappers {

  /**
   * Excepcion de post no encontrado.
   *
   * @param e excepcion de post no encontrado.
   * @return http 404 no encontrado.
   */
  @ServerExceptionMapper
  public Response mapNotFound(PostNotFoundException e) {
    return Response.status(Response.Status.NOT_FOUND)
        .entity(Map.of("error", e.getMessage()))
        .build();
  }

  /**
   * Maneja error de tiempo agotado por el provedor.
   *
   * @return http status 504 exception.
   */
  @ServerExceptionMapper(TimeoutException.class)
  public Response timeOutException() {
    return Response.status(Response.Status.GATEWAY_TIMEOUT)
        .entity(Map.of("error", "Tiempo de espera excedido"))
        .build();
  }
}
