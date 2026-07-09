package com.omar.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.omar.domain.exception.PostNotFoundException;
import com.omar.domain.model.Autor;
import com.omar.domain.model.Post;
import com.omar.domain.port.in.GetPostDetailUseCase;
import com.omar.domain.port.out.AutorProviderPort;
import com.omar.domain.port.out.PostProviderPort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PostServiceTest {

  @InjectMock PostProviderPort postProviderPort;
  @InjectMock AutorProviderPort autorProviderPort;
  @Inject GetPostDetailUseCase getDetail;

  @Test
  void detallePostConAutor() {
    var post = new Post(1L, "Algo", "Cuerpo", 1L);
    var autor = new Autor(1L, "Autor1");
    when(postProviderPort.fetchPost(1L)).thenReturn(Uni.createFrom().item(post));
    when(autorProviderPort.fetchAutor(1L)).thenReturn(Uni.createFrom().item(autor));
    var result = getDetail.getPostDetail(1L).await().atMost(Duration.ofSeconds(2));
    assertEquals("Autor1", result.autor().nombre());
    assertEquals(1L, result.post().id());
  }

  @Test
  void fallaExcepcionSiNoHayPost() {
    when(postProviderPort.fetchPost(99L)).thenReturn(Uni.createFrom().nullItem());

    assertThrows(
        PostNotFoundException.class,
        () -> getDetail.getPostDetail(99L).await().atMost(Duration.ofSeconds(2)));
  }
}
