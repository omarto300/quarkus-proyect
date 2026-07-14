package com.omar.infrastructure.adapter.out.openweathermap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpenWeatherMapMainDto {

  private double temp;

  @JsonProperty("feels_like")
  private double feelsLike;

  private int humidity;
}
