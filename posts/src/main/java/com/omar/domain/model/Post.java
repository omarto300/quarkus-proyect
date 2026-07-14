package com.omar.domain.model;

/**
 * Post clase que represtan un post.
 *
 * @param id del post.
 * @param titulo del post.
 * @param cuerpo del post.
 * @param idAutor referencia a Autor con id.
 */
public record Post(Long id, String titulo, String cuerpo, Long idAutor) {
  public boolean isLargePost() {
    return cuerpo != null && cuerpo.length() >= 100;
  }

  /**
   * Metodo de comprobacion de relacion post con autor.
   *
   * @param autor del post a verificar.
   * @return autor pertence al post.
   */
  public boolean belongsTo(Autor autor) {
    return autor != null && autor.id().equals(idAutor);
  }
}
