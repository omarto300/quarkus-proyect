package com.omar.infrastructure.adapter.in.rest;

import com.omar.domain.port.in.GetPostDetailUseCase;
import com.omar.domain.port.in.GetPostUseCase;
import com.omar.infrastructure.adapter.in.rest.dto.PostDetailResponse;
import com.omar.infrastructure.adapter.in.rest.dto.PostResponse;
import com.omar.infrastructure.adapter.in.rest.mapper.PostRestMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/v1/posts")
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

  private final GetPostUseCase postUseCase;
  private final GetPostDetailUseCase postDetailUseCase;
  private final PostRestMapper mapper;

  public PostResource(
      GetPostUseCase postUseCase, GetPostDetailUseCase postDetailUseCase, PostRestMapper mapper) {
    this.postUseCase = postUseCase;
    this.postDetailUseCase = postDetailUseCase;
    this.mapper = mapper;
  }

  @GET
  public Uni<List<PostResponse>> getAll() {
    return postUseCase.getAllPosts().map(mapper::toResponseList);
  }

  @GET
  @Path("/{id}")
  public Uni<PostDetailResponse> getOne(@PathParam("id") Long id) {
    return postDetailUseCase.getPostDetail(id).map(mapper::toResponse);
  }
}
