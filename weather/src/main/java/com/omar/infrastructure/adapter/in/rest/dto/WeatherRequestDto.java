package com.omar.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

public record WeatherRequestDto(@NotNull Double latitude, @NotNull Double longitude) {
}
