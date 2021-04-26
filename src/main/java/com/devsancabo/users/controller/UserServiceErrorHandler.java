package com.devsancabo.users.controller;

import com.devsancabo.users.exception.ApiError;
import com.devsancabo.users.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class UserServiceErrorHandler extends ResponseEntityExceptionHandler {
//TODO: Segregate http error codes
  @ExceptionHandler(value
    = { ApiException.class})
  protected ResponseEntity<ApiError> handleApiException(final ApiException ex, final WebRequest request){
      return ResponseEntity.badRequest().body(ex.getError());
  }

  @ExceptionHandler(value
    = { RuntimeException.class})
  protected ResponseEntity<ApiError> handleApiException(final RuntimeException ex, final WebRequest request){
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError("Internal Server Error"));
  }
}
