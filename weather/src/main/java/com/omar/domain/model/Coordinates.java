package com.omar.domain.model;

import com.omar.domain.exception.InvalidCoordinatesException;

public record Coordinates(double lat, double lon) {
  public Coordinates {
    if (lat < -90 || lat > 90) {
      throw new InvalidCoordinatesException("Latitud fuera de rango (-90..90): " + lat);
    }
    if (lon < -180 || lon > 180) {
      throw new InvalidCoordinatesException("Longitud fuera de rango (-180..180): " + lon);
    }
  }
}
