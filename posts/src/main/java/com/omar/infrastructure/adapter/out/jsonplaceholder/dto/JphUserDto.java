package com.omar.infrastructure.adapter.out.jsonplaceholder.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JphUserDto {

  private Long id;
  private String name;
  private String username;
  private String email;
}
