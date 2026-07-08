package com.omar.domain.port.in;

import com.omar.domain.model.PostDetail;
import io.smallrye.mutiny.Uni;

public interface GetPostDetailUseCase {

  Uni<PostDetail> getPostDetail(Long id);
}
