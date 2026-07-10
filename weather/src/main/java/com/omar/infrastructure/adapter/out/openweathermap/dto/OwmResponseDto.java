package com.omar.infrastructure.adapter.out.openweathermap.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OwmResponseDto {

  private String name;
  private List<OwmWeatherDetailDto> weather;
  private OwmMainDto main;
  private OwmWindDto wind;
}
