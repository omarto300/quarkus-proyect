package com.omar.domain.port.out;

import com.omar.domain.model.Post;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface PostProviderPort {
  Uni<List<Post>> fetchPosts();

  Uni<Post> fetchPost(Long id);
}
