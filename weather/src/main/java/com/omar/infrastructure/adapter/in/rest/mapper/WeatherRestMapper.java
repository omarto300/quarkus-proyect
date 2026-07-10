package com.omar.infrastructure.adapter.in.rest.mapper;

import com.omar.domain.model.Weather;
import com.omar.infrastructure.adapter.in.rest.dto.WeatherResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface WeatherRestMapper {

  WeatherResponse toResponse(Weather weather);
}
