package com.omar.infrastructure.adapter.out.openweathermap.mapper;

import com.omar.domain.model.Weather;
import com.omar.infrastructure.adapter.out.openweathermap.dto.OwmResponseDto;
import com.omar.infrastructure.adapter.out.openweathermap.dto.OwmWeatherDetailDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OpenWeatherMapper {

  @Mapping(source = "name", target = "city")
  @Mapping(target = "description", expression = "java(firstDescription(dto.getWeather()))")
  @Mapping(source = "main.temp", target = "temperature")
  @Mapping(source = "main.feelsLike", target = "feelsLike")
  @Mapping(source = "main.humidity", target = "humidity")
  @Mapping(source = "wind.speed", target = "windSpeed")
  Weather toDomain(OwmResponseDto dto);

  default String firstDescription(List<OwmWeatherDetailDto> weather) {
    return weather == null || weather.isEmpty() ? "" : weather.get(0).getDescription();
  }
}
