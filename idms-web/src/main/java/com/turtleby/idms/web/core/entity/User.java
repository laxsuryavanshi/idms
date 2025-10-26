package com.turtleby.idms.web.core.entity;

import java.io.Serial;
import java.io.Serializable;

public record User(String id, String username, String password, boolean isActive)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
