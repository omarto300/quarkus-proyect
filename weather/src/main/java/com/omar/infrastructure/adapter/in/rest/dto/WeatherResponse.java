package com.omar.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherResponse(
    @JsonProperty("ciudad") String city,
    @JsonProperty("descripcion") String description,
    @JsonProperty("temperatura") double temperature,
    @JsonProperty("sensacionTermica") double feelsLike,
    @JsonProperty("humedad") int humidity,
    @JsonProperty("velocidadViento") double windSpeed) {}
