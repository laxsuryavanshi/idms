package com.turtleby.idms.web.tenant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.turtleby.idms.web.common.exception.EntityNotFoundException;
import com.turtleby.idms.web.tenant.dao.TenantRepository;
import com.turtleby.idms.web.tenant.dto.TenantCreateRequest;
import com.turtleby.idms.web.tenant.dto.TenantResponse;
import com.turtleby.idms.web.tenant.dto.TenantUpdateRequest;
import com.turtleby.idms.web.tenant.entity.Tenant;
import com.turtleby.idms.web.tenant.mapping.TenantMapper;
import com.turtleby.idms.web.user.entity.User;

public class TenantService implements TenantManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(TenantService.class);

  private final TenantRepository tenantRepository;
  private final TenantMapper tenantMapper = TenantMapper.INSTANCE;

  public TenantService(final TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  public Long createTenant(final TenantCreateRequest request, final User user) {
    final Tenant tenant = tenantMapper.toEntity(request, user);

    final Tenant savedTenant = tenantRepository.save(tenant);
    LOGGER.debug("Created new tenant with ID {} and slug {}", savedTenant.id(), savedTenant.slug());

    return savedTenant.id();
  }

  public Page<TenantResponse> listTenants(final Pageable pageable) {
    final Page<Tenant> tenants = tenantRepository.findAll(pageable);
    return tenants.map(tenantMapper::toResponse);
  }

  public TenantResponse getTenantById(final Long tenantId) {
    return tenantRepository
        .findById(tenantId)
        .map(tenantMapper::toResponse)
        .orElseThrow(() -> notFoundException(tenantId));
  }

  public TenantResponse getTenantBySlug(final String slug) {
    return tenantRepository
        .findBySlug(slug)
        .map(tenantMapper::toResponse)
        .orElseThrow(() -> notFoundException("slug", slug));
  }

  @Transactional
  public TenantResponse updateTenantById(final Long tenantId, final TenantUpdateRequest request) {
    final Tenant tenant =
        tenantRepository.findById(tenantId).orElseThrow(() -> notFoundException(tenantId));

    Tenant updatedTenant = updateTenant(tenant, request);
    LOGGER.debug("Updated tenant with ID {}", tenantId);
    return tenantMapper.toResponse(updatedTenant);
  }

  private Tenant updateTenant(final Tenant tenant, final TenantUpdateRequest request) {
    final Tenant updatedTenant = tenantMapper.updateFromRequest(tenant, request);
    return tenantRepository.save(updatedTenant);
  }

  @Transactional
  public void deleteTenantById(final Long tenantId) {
    if (!tenantRepository.existsById(tenantId)) {
      throw notFoundException(tenantId);
    }

    tenantRepository.deleteById(tenantId);
    LOGGER.debug("Deleted tenant with ID {}", tenantId);
  }

  public boolean existsBySlug(final String slug) {
    return tenantRepository.existsBySlug(slug);
  }

  private EntityNotFoundException notFoundException(final Long tenantId) {
    return notFoundException("ID", tenantId);
  }

  private EntityNotFoundException notFoundException(final String field, final Object value) {
    return new EntityNotFoundException(
        String.format("Tenant with %s '%s' not found", field, value));
  }
}
