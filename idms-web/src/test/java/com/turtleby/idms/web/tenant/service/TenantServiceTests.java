package com.turtleby.idms.web.tenant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import com.turtleby.idms.web.common.exception.EntityNotFoundException;
import com.turtleby.idms.web.core.entity.User;
import com.turtleby.idms.web.tenant.dao.TenantRepository;
import com.turtleby.idms.web.tenant.dto.TenantCreateRequest;
import com.turtleby.idms.web.tenant.dto.TenantResponse;
import com.turtleby.idms.web.tenant.dto.TenantUpdateRequest;
import com.turtleby.idms.web.tenant.entity.Tenant;
import com.turtleby.idms.web.tenant.entity.TenantStatus;
import com.turtleby.idms.web.tenant.entity.TenantType;

@ExtendWith(MockitoExtension.class)
class TenantServiceTests {
  @Mock private TenantRepository tenantRepository;

  private TenantService tenantService;

  private User testUser;
  private Tenant testTenant;
  private TenantCreateRequest createRequest;
  private TenantUpdateRequest updateRequest;

  @BeforeEach
  void setUp() {
    tenantService = new TenantService(tenantRepository);

    testUser = new User("user-123", "testuser", "password", true);

    testTenant =
        new Tenant(
            1L,
            "Test Tenant",
            "test-tenant",
            TenantType.STANDARD,
            TenantStatus.ACTIVE,
            "user-123",
            Instant.now(),
            Instant.now());

    createRequest = new TenantCreateRequest("Test Tenant", "test-tenant");
    updateRequest = new TenantUpdateRequest("Updated Tenant");
  }

  @Nested
  class CreateTenant {
    @Test
    void shouldSaveTenantAndReturnId() {
      // Given
      Tenant savedTenant =
          new Tenant(
              1L,
              "Test Tenant",
              "test-tenant",
              TenantType.STANDARD,
              TenantStatus.ACTIVE,
              "user-123",
              Instant.now(),
              Instant.now());
      when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

      // When
      Long tenantId = tenantService.createTenant(createRequest, testUser);

      // Then
      assertThat(tenantId).isEqualTo(1L);
      verify(tenantRepository).save(any(Tenant.class));
    }
  }

  @Nested
  class ListTenants {
    @Test
    void shouldReturnPagedTenants() {
      // Given
      Pageable pageable = PageRequest.of(0, 10);
      List<Tenant> tenants = List.of(testTenant);
      Page<Tenant> tenantPage = new PageImpl<>(tenants, pageable, 1);
      when(tenantRepository.findAll(pageable)).thenReturn(tenantPage);

      // When
      Page<TenantResponse> result = tenantService.listTenants(pageable);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).name()).isEqualTo("Test Tenant");
      assertThat(result.getContent().get(0).slug()).isEqualTo("test-tenant");
      verify(tenantRepository).findAll(pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenNoTenants() {
      // Given
      Pageable pageable = PageRequest.of(0, 10);
      Page<Tenant> emptyPage = new PageImpl<>(List.of(), pageable, 0);
      when(tenantRepository.findAll(pageable)).thenReturn(emptyPage);

      // When
      Page<TenantResponse> result = tenantService.listTenants(pageable);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  class GetTenantById {
    @Test
    void shouldReturnTenantWhenExists() {
      // Given
      when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));

      // When
      TenantResponse result = tenantService.getTenantById(1L);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.name()).isEqualTo("Test Tenant");
      assertThat(result.slug()).isEqualTo("test-tenant");
      assertThat(result.status()).isEqualTo(TenantStatus.ACTIVE);
      verify(tenantRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
      // Given
      when(tenantRepository.findById(1L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> tenantService.getTenantById(1L))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Tenant with ID '1' not found");
    }
  }

  @Nested
  class GetTenantBySlug {
    @Test
    void shouldReturnTenantWhenExists() {
      // Given
      when(tenantRepository.findBySlug("test-tenant")).thenReturn(Optional.of(testTenant));

      // When
      TenantResponse result = tenantService.getTenantBySlug("test-tenant");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.slug()).isEqualTo("test-tenant");
      assertThat(result.name()).isEqualTo("Test Tenant");
      verify(tenantRepository).findBySlug("test-tenant");
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
      // Given
      when(tenantRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> tenantService.getTenantBySlug("non-existent"))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Tenant with slug 'non-existent' not found");
    }
  }

  @Nested
  class UpdateTenantById {
    @Test
    void shouldUpdateAndReturnTenant() {
      // Given
      Tenant updatedTenant =
          new Tenant(
              1L,
              "Updated Tenant",
              "test-tenant",
              TenantType.STANDARD,
              TenantStatus.ACTIVE,
              "user-123",
              testTenant.createdAt(),
              Instant.now());

      when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
      when(tenantRepository.save(any(Tenant.class))).thenReturn(updatedTenant);

      // When
      TenantResponse result = tenantService.updateTenantById(1L, updateRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.name()).isEqualTo("Updated Tenant");
      verify(tenantRepository).findById(1L);
      verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void shouldThrowExceptionWhenTenantNotFound() {
      // Given
      when(tenantRepository.findById(1L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> tenantService.updateTenantById(1L, updateRequest))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Tenant with ID '1' not found");
      verify(tenantRepository).findById(1L);
      verify(tenantRepository, never()).save(any(Tenant.class));
    }
  }

  @Nested
  class DeleteTenantById {
    @Test
    void shouldDeleteTenantWhenExists() {
      // Given
      when(tenantRepository.existsById(1L)).thenReturn(true);

      // When
      tenantService.deleteTenantById(1L);

      // Then
      verify(tenantRepository).existsById(1L);
      verify(tenantRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTenantNotFound() {
      // Given
      when(tenantRepository.existsById(1L)).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> tenantService.deleteTenantById(1L))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Tenant with ID '1' not found");
      verify(tenantRepository).existsById(1L);
      verify(tenantRepository, never()).deleteById(1L);
    }
  }

  @Nested
  class ExistsBySlug {
    @Test
    void shouldReturnTrueWhenSlugExists() {
      // Given
      when(tenantRepository.existsBySlug("test-tenant")).thenReturn(true);

      // When
      boolean exists = tenantService.existsBySlug("test-tenant");

      // Then
      assertThat(exists).isTrue();
      verify(tenantRepository).existsBySlug("test-tenant");
    }

    @Test
    void shouldReturnFalseWhenSlugDoesNotExist() {
      // Given
      when(tenantRepository.existsBySlug("non-existent")).thenReturn(false);

      // When
      boolean exists = tenantService.existsBySlug("non-existent");

      // Then
      assertThat(exists).isFalse();
      verify(tenantRepository).existsBySlug("non-existent");
    }
  }
}
