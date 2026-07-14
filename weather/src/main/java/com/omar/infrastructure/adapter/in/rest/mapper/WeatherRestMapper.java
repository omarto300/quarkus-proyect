package com.omar.infrastructure.adapter.in.rest.mapper;

import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import com.omar.infrastructure.adapter.in.rest.dto.WeatherRequestDto;
import com.omar.infrastructure.adapter.in.rest.dto.WeatherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface WeatherRestMapper {

  WeatherResponse toResponse(Weather weather);

  @Mapping(source = "latitude",target = "lat")
  @Mapping(source = "longitude", target = "lon")
  Coordinates toCoordinates(WeatherRequestDto weatherRequestDto);
}
