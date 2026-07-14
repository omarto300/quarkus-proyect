package com.omar.infrastructure.adapter.out.openweathermap.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.omar.infrastructure.adapter.out.openweathermap.dto.OpenWeatherMapMainDto;
import com.omar.infrastructure.adapter.out.openweathermap.dto.OpenWeatherMapResponseDto;
import com.omar.infrastructure.adapter.out.openweathermap.dto.OpenWeatherMapWindDto;
import org.junit.jupiter.api.Test;

class OpenWeatherMapperUnitTest {

  private final OpenWeatherMapper mapper = new OpenWeatherMapperImpl();

  @Test
  void shouldReturnZeroValuesWhenMainDtoIsNull() {
    var dto = new OpenWeatherMapResponseDto();
    dto.setName("City");
    dto.setMain(null);
    var wind = new OpenWeatherMapWindDto();
    wind.setSpeed(3.0);
    dto.setWind(wind);

    var result = mapper.toDomain(dto);

    assertEquals(0.0, result.temperature());
    assertEquals(0.0, result.feelsLike());
    assertEquals(0, result.humidity());
  }

  @Test
  void shouldReturnZeroWindSpeedWhenWindDtoIsNull() {
    var main = new OpenWeatherMapMainDto();
    main.setTemp(20.0);
    main.setFeelsLike(19.0);
    main.setHumidity(70);

    var dto = new OpenWeatherMapResponseDto();
    dto.setName("City");
    dto.setMain(main);
    dto.setWind(null);

    var result = mapper.toDomain(dto);

    assertEquals(0.0, result.windSpeed());
    assertEquals(20.0, result.temperature());
  }
}
