package com.turtleby.idms.web.tenant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.turtleby.idms.web.tenant.dto.TenantCreateRequest;
import com.turtleby.idms.web.tenant.dto.TenantResponse;
import com.turtleby.idms.web.tenant.dto.TenantUpdateRequest;
import com.turtleby.idms.web.user.entity.User;

public interface TenantManager {
  Long createTenant(final TenantCreateRequest request, User user);

  Page<TenantResponse> listTenants(final Pageable pageable);

  TenantResponse getTenantById(final Long tenantId);

  TenantResponse getTenantBySlug(final String slug);

  TenantResponse updateTenantById(final Long tenantId, final TenantUpdateRequest request);

  void deleteTenantById(final Long tenantId);

  boolean existsBySlug(final String slug);
}
