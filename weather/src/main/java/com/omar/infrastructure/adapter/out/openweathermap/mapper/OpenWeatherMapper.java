package com.omar.infrastructure.adapter.out.openweathermap.mapper;

import com.omar.domain.model.Weather;
import com.omar.infrastructure.adapter.out.openweathermap.dto.OpenWeatherMapConditionDto;
import com.omar.infrastructure.adapter.out.openweathermap.dto.OpenWeatherMapResponseDto;
import java.util.List;
import java.util.Optional;

import io.smallrye.config.common.utils.StringUtil;
import io.vertx.ext.web.handler.sockjs.impl.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
  Weather toDomain(OpenWeatherMapResponseDto dto);

  default String firstDescription(List<OpenWeatherMapConditionDto> weather) {
    return Optional.ofNullable(weather)
        .stream()
        .flatMap(List::stream)
        .map(OpenWeatherMapConditionDto::getDescription)
        .findFirst()
        .orElse(StringUtils.EMPTY);
  }
}
