package com.omar.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PostTest {

  @Test
  void shortPostTest() {
    var post = new Post(1L, "Titulo", "C".repeat(99), 1L);
    assertFalse(post.isLargePost());
  }

  @Test
  void isLargePostTest() {
    var post = new Post(2L, "Large post", "x".repeat(100), 1L);

    assertTrue(post.isLargePost());
  }

  @Test
  void postPerteneceAutorCorrecto() {
    var post = new Post(1L, "Title", "Cuerpo", 1L);
    var autor = new Autor(1L, "Autor");
    assertTrue(post.belongsTo(autor));
  }

  @Test
  void postNoPertenceAutor() {
    var post = new Post(1L, "Title", "cuerpo", 2L);
    var autor = new Autor(1L, "Autor");

    assertThrows(IllegalArgumentException.class, () -> new PostDetail(post, autor));
  }

  @Test
  void postCreateNotError() {
    assertDoesNotThrow(() -> new PostDetail(new Post(1L, "t", "c", 1L), new Autor(1L, "Autor")));
  }
}
