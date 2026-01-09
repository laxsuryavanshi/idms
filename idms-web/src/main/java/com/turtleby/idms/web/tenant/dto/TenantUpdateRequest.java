package com.turtleby.idms.web.tenant.dto;

import jakarta.validation.constraints.NotBlank;

public record TenantUpdateRequest(@NotBlank String name) {}
