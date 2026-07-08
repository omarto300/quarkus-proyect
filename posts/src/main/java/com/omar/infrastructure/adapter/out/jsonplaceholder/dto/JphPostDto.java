package com.omar.infrastructure.adapter.out.jsonplaceholder.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JphPostDto {

  private Long id;
  private Long userId;
  private String title;
  private String body;
}
