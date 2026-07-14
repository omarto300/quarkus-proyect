package com.omar.domain.port.in;

import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import io.smallrye.mutiny.Uni;

public interface GetWeatherUseCase {
  Uni<Weather> getWeather(Coordinates coordinates);
}
