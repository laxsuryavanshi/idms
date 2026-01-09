package com.turtleby.idms.web.tenant.dto;

import jakarta.validation.constraints.NotBlank;

public record TenantCreateRequest(@NotBlank String name, String slug) {}
