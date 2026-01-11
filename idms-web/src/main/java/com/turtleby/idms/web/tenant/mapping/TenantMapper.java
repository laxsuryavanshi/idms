package com.turtleby.idms.web.tenant.mapping;

import java.time.Instant;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.turtleby.idms.web.tenant.dto.TenantCreateRequest;
import com.turtleby.idms.web.tenant.dto.TenantResponse;
import com.turtleby.idms.web.tenant.dto.TenantUpdateRequest;
import com.turtleby.idms.web.tenant.entity.Tenant;
import com.turtleby.idms.web.tenant.entity.TenantStatus;
import com.turtleby.idms.web.tenant.entity.TenantType;
import com.turtleby.idms.web.user.entity.User;

@Mapper
public interface TenantMapper {
  TenantMapper INSTANCE = Mappers.getMapper(TenantMapper.class);

  TenantResponse toResponse(Tenant tenant);

  default Tenant toEntity(final TenantCreateRequest request, final User user) {
    return new Tenant(
        null,
        request.name(),
        request.slug(),
        TenantType.STANDARD,
        TenantStatus.ACTIVE,
        user.id(),
        Instant.now(),
        Instant.now());
  }

  default Tenant updateFromRequest(final Tenant tenant, final TenantUpdateRequest request) {
    return new Tenant(
        tenant.id(),
        request.name(),
        tenant.slug(),
        tenant.type(),
        tenant.status(),
        tenant.ownerId(),
        tenant.createdAt(),
        Instant.now());
  }
}
