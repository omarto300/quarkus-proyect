package com.omar.infrastructure.adapter.out.openweathermap.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpenWeatherMapResponseDto {

  private String name;
  private List<OpenWeatherMapConditionDto> weather;
  private OpenWeatherMapMainDto main;
  private OpenWeatherMapWindDto wind;
}
