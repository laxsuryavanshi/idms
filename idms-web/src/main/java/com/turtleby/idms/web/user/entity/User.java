package com.turtleby.idms.web.user.entity;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Builder
@Table("users")
public record User(@Id String id, String username, String password, boolean isActive)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
