package com.omar.domain.exception;

public class WeatherServiceUnavailableException extends RuntimeException {

  public WeatherServiceUnavailableException(String message) {
    super(message);
  }
}
