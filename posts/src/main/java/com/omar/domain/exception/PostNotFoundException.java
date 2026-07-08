package com.omar.domain.exception;

public class PostNotFoundException extends RuntimeException {

  public PostNotFoundException(Long id) {
    super("Post no encontrado: " + id);
  }
}
