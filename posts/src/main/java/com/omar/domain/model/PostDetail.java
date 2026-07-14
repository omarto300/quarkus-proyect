package com.omar.domain.model;

/**
 * Detalle del post con su autor.
 *
 * @param post con titulo y cuerpo.
 * @param autor con id y nombre.
 */
public record PostDetail(Post post, Autor autor) {
  /**
   * Constructor del record para que se cree un detalle de post y autor.
   *
   * @param post a analizar.
   * @param autor autor del post obtenido.
   */
  public PostDetail {
    if (!post.belongsTo(autor)) {
      throw new IllegalArgumentException(
          "El autor %d no corresponde al post %d".formatted(autor.id(), post.id()));
    }
  }
}
