package com.omar.infrastructure.adapter.in.rest.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostDetailResponse {

  private Long id;
  private String title;
  private String body;
  private boolean isLargePost;
  private String author;
}
