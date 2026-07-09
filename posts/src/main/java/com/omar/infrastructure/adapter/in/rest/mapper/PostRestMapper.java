package com.omar.infrastructure.adapter.in.rest.mapper;

import com.omar.domain.model.Post;
import com.omar.domain.model.PostDetail;
import com.omar.infrastructure.adapter.in.rest.dto.PostDetailResponse;
import com.omar.infrastructure.adapter.in.rest.dto.PostResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface PostRestMapper {

  @Mapping(source = "titulo", target = "title")
  @Mapping(source = "cuerpo", target = "body")
  @Mapping(target = "isLargePost", expression = "java(post.isLargePost())")
  PostResponse toResponse(Post post);

  List<PostResponse> toResponseList(List<Post> posts);

  @Mapping(source = "post.id", target = "id")
  @Mapping(source = "post.titulo", target = "title")
  @Mapping(source = "post.cuerpo", target = "body")
  @Mapping(target = "isLargePost", expression = "java(postDetail.post().isLargePost())")
  @Mapping(source = "autor.nombre", target = "author")
  PostDetailResponse toResponse(PostDetail postDetail);
}
