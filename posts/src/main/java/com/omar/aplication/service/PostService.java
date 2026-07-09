package com.omar.aplication.service;

import com.omar.domain.exception.PostNotFoundException;
import com.omar.domain.model.Post;
import com.omar.domain.model.PostDetail;
import com.omar.domain.port.in.GetPostDetailUseCase;
import com.omar.domain.port.in.GetPostUseCase;
import com.omar.domain.port.out.AutorProviderPort;
import com.omar.domain.port.out.PostProviderPort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PostService implements GetPostUseCase, GetPostDetailUseCase {
  private final PostProviderPort postProvider;
  private final AutorProviderPort autorProvider;

  public PostService(PostProviderPort postProviderPort, AutorProviderPort autorProviderPort) {
    this.postProvider = postProviderPort;
    this.autorProvider = autorProviderPort;
  }

  @Override
  public Uni<List<Post>> getAllPosts() {
    return postProvider.fetchPosts();
  }

  @Override
  public Uni<PostDetail> getPostDetail(Long id) {
    return postProvider
        .fetchPost(id)
        .onItem()
        .ifNull()
        .failWith(() -> new PostNotFoundException(id))
        .flatMap(
            post ->
                autorProvider.fetchAutor(post.idAutor()).map(autor -> new PostDetail(post, autor)));
  }
}
