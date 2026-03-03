package com.turtleby.idms.web.user.entity;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Builder
@Table("user_email_address")
public record EmailAddress(
    @Id Long id, String userId, String email, boolean isPrimary, boolean isVerified)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
