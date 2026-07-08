package com.omar.domain.model;

public record PostDetail(Post post, Autor autor) {
  public PostDetail {
    if (!post.belongsTo(autor)) {
      throw new IllegalArgumentException(
          "El autor %d no corresponde al post %d".formatted(autor.id(), post.idPost()));
    }
  }
}
