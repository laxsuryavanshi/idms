package com.turtleby.idms.web.user.entity;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Builder
@Table("user_phone_number")
public record PhoneNumber(
    @Id String id, String userId, String phoneNumber, boolean isPrimary, boolean isVerified)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
