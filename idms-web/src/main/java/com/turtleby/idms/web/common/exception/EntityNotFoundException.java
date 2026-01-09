package com.turtleby.idms.web.common.exception;

import java.io.Serial;

public class EntityNotFoundException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  private static final String DEFAULT_MESSAGE = "Entity not found";
  private static final String DEFAULT_CODE = "ENTITY_NOT_FOUND";

  private final String code;

  public EntityNotFoundException() {
    this(DEFAULT_MESSAGE);
  }

  public EntityNotFoundException(String message) {
    this(message, DEFAULT_CODE);
  }

  public EntityNotFoundException(String message, String code) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
