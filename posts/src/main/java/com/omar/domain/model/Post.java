package com.omar.domain.model;

public record Post(Long idPost, String titulo, String cuerpo, Long idAutor) {
  public boolean isLargePost() {
    return cuerpo != null && cuerpo.length() > 200;
  }

  public boolean belongsTo(Autor autor) {
    return autor != null && autor.id().equals(idAutor);
  }
}
