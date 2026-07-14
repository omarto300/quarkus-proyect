package com.omar.domain.exception;

/**
 * Excepcion de post no encontrado.
 */
public class PostNotFoundException extends RuntimeException {

  /**
   * Constructs de un PostNotFoundException cuando se le pasa un ID.
   *
   * @param id el id de post no encontrado
   */
  public PostNotFoundException(Long id) {
    super("Post no encontrado: " + id);
  }
}
