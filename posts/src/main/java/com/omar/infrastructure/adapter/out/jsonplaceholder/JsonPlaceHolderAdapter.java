package com.omar.infrastructure.adapter.out.jsonplaceholder;

import com.omar.domain.model.Autor;
import com.omar.domain.model.Post;
import com.omar.domain.port.out.AutorProviderPort;
import com.omar.domain.port.out.PostProviderPort;
import com.omar.infrastructure.adapter.out.jsonplaceholder.mapper.JsonPlaceHolderMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@ApplicationScoped
public class JsonPlaceHolderAdapter implements PostProviderPort, AutorProviderPort {

  @Inject @RestClient JsonPlaceHolderClient jsonPlaceHolderClient;

  @Inject
  JsonPlaceHolderMapper jsonPlaceHolderMapper;

  @Override
  @Timeout(3000)
  @Retry(maxRetries = 2, delay = 200, abortOn = TimeoutException.class)
  public Uni<Post> fetchPost(Long id) {
    return jsonPlaceHolderClient.getPost(id)
            .map(jsonPlaceHolderMapper::toDomain)
            .onFailure(this::isNotFound).recoverWithNull();
  }

  @Override
  @Timeout(3000)
  @Retry(maxRetries = 2, delay = 200)
  @Fallback(fallbackMethod = "fetchPostsFallback")
  public Uni<List<Post>> fetchPosts() {
    return jsonPlaceHolderClient.getPosts().map(jsonPlaceHolderMapper::toDomainList);
  }

  @Override
  @Timeout(3000)
  @Retry(maxRetries = 2, delay = 200)
  public Uni<Autor> fetchAutor(Long id) {
    return jsonPlaceHolderClient.getUser(id)
        .map(jsonPlaceHolderMapper::toDomain)
            .onFailure(this::isNotFound)
        .recoverWithNull();
  }

  Uni<List<Post>> fetchPostsFallback() {
    return Uni.createFrom().item(List.of());
  }

  private boolean isNotFound(Throwable throwable) {
    return throwable instanceof ClientWebApplicationException e && e.getResponse().getStatus() == 404;
  }
}
