package com.omar.domain.port.in;

import com.omar.domain.model.Post;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface GetPostUseCase {
  Uni<List<Post>> getAllPosts();
}
