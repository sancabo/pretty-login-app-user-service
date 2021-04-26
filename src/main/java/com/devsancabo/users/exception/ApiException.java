package com.devsancabo.users.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
  final ApiError error;
  public ApiException(String message) {
    super(message);
    this.error = new ApiError(message);
  }
}
