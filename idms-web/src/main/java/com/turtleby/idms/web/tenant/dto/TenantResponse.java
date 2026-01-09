package com.turtleby.idms.web.tenant.dto;

import java.time.Instant;

import com.turtleby.idms.web.tenant.entity.TenantStatus;
import com.turtleby.idms.web.tenant.entity.TenantType;

public record TenantResponse(
    Long id,
    String name,
    String slug,
    TenantType type,
    TenantStatus status,
    String ownerId,
    Instant createdAt,
    Instant updatedAt) {}
