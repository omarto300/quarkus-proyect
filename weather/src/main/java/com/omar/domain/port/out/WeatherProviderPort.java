package com.omar.domain.port.out;

import com.omar.domain.model.Coordinates;
import com.omar.domain.model.Weather;
import io.smallrye.mutiny.Uni;

public interface WeatherProviderPort {
  Uni<Weather> fetchWeather(Coordinates coordinates);
}
