package com.turtleby.idms.web.user.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;

@Builder
@Table("user_account")
public record UserAccount(
    @Id Long id,
    String userId,
    AuthenticationProviderType providerType,
    String provider,
    String providerAccountId,
    Instant createdAt,
    Instant updatedAt) {}
