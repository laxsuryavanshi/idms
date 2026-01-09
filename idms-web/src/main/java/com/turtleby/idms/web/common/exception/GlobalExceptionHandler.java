package com.turtleby.idms.web.common.exception;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.turtleby.idms.web.common.dto.ErrorResponse;

/**
 * Global exception handler for REST controllers.
 *
 * <p>Handles common exceptions and maps them to appropriate HTTP responses with problem details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
    return buildErrorResponse(ex.getCode(), HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    Map<String, List<String>> validationErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

    return buildErrorResponse(
        "VALIDATION_ERROR",
        HttpStatus.BAD_REQUEST,
        "Validation failed for one or more fields",
        new HashMap<>(validationErrors));
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      String code, HttpStatus status, String message) {
    return buildErrorResponse(code, status, message, Collections.emptyMap());
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      String code, HttpStatus status, String message, Map<String, Object> details) {
    ErrorResponse response = new ErrorResponse(code, status.value(), message, details);

    return ResponseEntity.status(status).body(response);
  }
}
