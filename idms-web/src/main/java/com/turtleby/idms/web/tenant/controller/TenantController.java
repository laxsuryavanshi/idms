package com.turtleby.idms.web.tenant.controller;

import static com.turtleby.idms.web.common.PaginationConstants.DEFAULT_PAGE_SIZE;
import static com.turtleby.idms.web.common.URIConstants.TENANTS_RESOURCE_URI;
import static com.turtleby.idms.web.common.URIConstants.TENANTS_RESOURCE_URI_SUFFIX;
import static com.turtleby.idms.web.common.URIConstants.TENANTS_URI_FORMAT;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.turtleby.idms.web.security.userdetails.SecurityUser;
import com.turtleby.idms.web.tenant.dto.TenantCreateRequest;
import com.turtleby.idms.web.tenant.dto.TenantResponse;
import com.turtleby.idms.web.tenant.dto.TenantUpdateRequest;
import com.turtleby.idms.web.tenant.service.TenantManager;

@RestController
@RequestMapping(path = TENANTS_RESOURCE_URI)
public class TenantController {
  private final TenantManager tenantManager;

  public TenantController(final TenantManager tenantManager) {
    this.tenantManager = tenantManager;
  }

  @PostMapping
  public ResponseEntity<Void> createTenant(
      @Valid @RequestBody final TenantCreateRequest request,
      @AuthenticationPrincipal final SecurityUser securityUser) {
    final Long tenantId = tenantManager.createTenant(request, securityUser.getUser());
    final URI location = URI.create(String.format(TENANTS_URI_FORMAT, tenantId));
    return ResponseEntity.created(location).build();
  }

  @GetMapping
  public Page<TenantResponse> listTenants(
      @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {
    return tenantManager.listTenants(pageable);
  }

  @GetMapping(params = "slug")
  public TenantResponse getTenantBySlug(@RequestParam final String slug) {
    return tenantManager.getTenantBySlug(slug);
  }

  @GetMapping(TENANTS_RESOURCE_URI_SUFFIX)
  public TenantResponse getTenantById(@PathVariable final Long tenantId) {
    return tenantManager.getTenantById(tenantId);
  }

  @PutMapping(TENANTS_RESOURCE_URI_SUFFIX)
  public TenantResponse updateTenantById(
      @PathVariable final Long tenantId, @Valid @RequestBody final TenantUpdateRequest request) {
    return tenantManager.updateTenantById(tenantId, request);
  }

  @DeleteMapping(TENANTS_RESOURCE_URI_SUFFIX)
  public ResponseEntity<Void> deleteTenantById(@PathVariable final Long tenantId) {
    tenantManager.deleteTenantById(tenantId);
    return ResponseEntity.noContent().build();
  }
}
