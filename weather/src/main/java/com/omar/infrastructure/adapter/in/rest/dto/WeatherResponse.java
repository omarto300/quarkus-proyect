package com.omar.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WeatherResponse {

  @JsonProperty("ciudad")
  private String city;

  @JsonProperty("descripcion")
  private String description;

  @JsonProperty("temperatura")
  private double temperature;

  @JsonProperty("sensacionTermica")
  private double feelsLike;

  @JsonProperty("humedad")
  private int humidity;

  @JsonProperty("velocidadViento")
  private double windSpeed;
}
