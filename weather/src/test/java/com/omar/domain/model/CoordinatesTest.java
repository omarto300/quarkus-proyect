package com.omar.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.omar.domain.exception.InvalidCoordinatesException;
import org.junit.jupiter.api.Test;

class CoordinatesTest {

  @Test
  void shouldAcceptBoundaryValues() {
    assertDoesNotThrow(() -> new Coordinates(90, 180));
    assertDoesNotThrow(() -> new Coordinates(-90, -180));
  }

  @Test
  void shouldRejectLatitudeOutOfRange() {
    assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(90.1, 0));
    assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(-90.1, 0));
  }

  @Test
  void shouldRejectLongitudeOutOfRange() {
    assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(0, 180.1));
    assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(0, -180.1));
  }
}
