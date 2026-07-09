package com.omar.infrastructure.adapter.out.jsonplaceholder.mapper;

import com.omar.domain.model.Autor;
import com.omar.domain.model.Post;
import com.omar.domain.port.out.AutorProviderPort;
import com.omar.domain.port.out.PostProviderPort;
import com.omar.infrastructure.adapter.out.jsonplaceholder.JsonPlaceHolderClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class JsonPlaceHolderAdapter implements PostProviderPort, AutorProviderPort {

  @Inject @RestClient JsonPlaceHolderClient jsonPlaceHolderClient;

  @Inject JsonPlaceHolderMapper jsonPlaceHolderMapper;

  @Override
  public Uni<Post> fetchPost(Long id) {
    return jsonPlaceHolderClient.getPost(id).map(jsonPlaceHolderMapper::toDomain);
  }

  @Override
  public Uni<List<Post>> fetchPosts() {
    return jsonPlaceHolderClient.getPosts().map(jsonPlaceHolderMapper::toDomainList);
  }

  @Override
  public Uni<Autor> fetchAutor(Long id) {
    return jsonPlaceHolderClient.getUser(id).map(jsonPlaceHolderMapper::toDomain);
  }
}
