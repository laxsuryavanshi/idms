package com.turtleby.idms.web.tenant.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("tenant")
public record Tenant(
    @Id Long id,
    String name,
    String slug,
    TenantType type,
    TenantStatus status,
    String ownerId,
    Instant createdAt,
    Instant updatedAt)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
