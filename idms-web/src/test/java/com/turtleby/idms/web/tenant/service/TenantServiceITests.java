package com.turtleby.idms.web.tenant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import com.turtleby.idms.web.TestcontainersConfiguration;
import com.turtleby.idms.web.common.exception.EntityNotFoundException;
import com.turtleby.idms.web.core.entity.User;
import com.turtleby.idms.web.tenant.dao.TenantRepository;
import com.turtleby.idms.web.tenant.dto.TenantCreateRequest;
import com.turtleby.idms.web.tenant.dto.TenantResponse;
import com.turtleby.idms.web.tenant.dto.TenantUpdateRequest;
import com.turtleby.idms.web.tenant.entity.Tenant;
import com.turtleby.idms.web.tenant.entity.TenantStatus;
import com.turtleby.idms.web.tenant.entity.TenantType;

@Import(TestcontainersConfiguration.class)
@DataJdbcTest
@DisplayName("TenantService Integration Tests")
class TenantServiceITests {
  @Autowired private TenantRepository tenantRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private TenantService tenantService;
  private User testUser;

  @BeforeEach
  void setUp() {
    tenantService = new TenantService(tenantRepository);

    // Create a test user in the database for foreign key constraints
    String userId = UUID.randomUUID().toString();
    testUser = new User(userId, "testuser", "password", true);
    jdbcTemplate.update(
        "INSERT INTO users (id, username, password, is_active) VALUES (?, ?, ?, ?)",
        userId,
        "testuser",
        "password",
        true);
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.execute("DELETE FROM tenant");
    jdbcTemplate.execute("DELETE FROM users");
  }

  @Nested
  @DisplayName("createTenant()")
  class CreateTenant {
    @Test
    @DisplayName("should create tenant with valid data")
    void shouldCreateTenantSuccessfully() {
      // Given
      TenantCreateRequest request = new TenantCreateRequest("Test Company", "test-company");

      // When
      Long tenantId = tenantService.createTenant(request, testUser);

      // Then
      assertThat(tenantId).isNotNull().isPositive();

      Tenant savedTenant = tenantRepository.findById(tenantId).orElseThrow();
      assertThat(savedTenant.name()).isEqualTo("Test Company");
      assertThat(savedTenant.slug()).isEqualTo("test-company");
      assertThat(savedTenant.ownerId()).isEqualTo(testUser.id());
      assertThat(savedTenant.type()).isEqualTo(TenantType.STANDARD);
      assertThat(savedTenant.status()).isEqualTo(TenantStatus.ACTIVE);
      assertThat(savedTenant.createdAt()).isNotNull();
      assertThat(savedTenant.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should create tenant with custom slug")
    void shouldCreateTenantWithCustomSlug() {
      // Given
      TenantCreateRequest request = new TenantCreateRequest("My Organization", "my-org");

      // When
      Long tenantId = tenantService.createTenant(request, testUser);

      // Then
      assertThat(tenantId).isNotNull();
      Tenant savedTenant = tenantRepository.findById(tenantId).orElseThrow();
      assertThat(savedTenant.slug()).isEqualTo("my-org");
    }

    @Test
    @DisplayName("should create multiple tenants for same owner")
    void shouldCreateMultipleTenantsForSameOwner() {
      // Given
      TenantCreateRequest request1 = new TenantCreateRequest("Company One", "company-one");
      TenantCreateRequest request2 = new TenantCreateRequest("Company Two", "company-two");

      // When
      Long tenantId1 = tenantService.createTenant(request1, testUser);
      Long tenantId2 = tenantService.createTenant(request2, testUser);

      // Then
      assertThat(tenantId1).isNotEqualTo(tenantId2);
      assertThat(tenantRepository.count()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("listTenants()")
  class ListTenants {
    @Test
    @DisplayName("should return empty page when no tenants exist")
    void shouldReturnEmptyPageWhenNoTenants() {
      // Given
      Pageable pageable = PageRequest.of(0, 10);

      // When
      Page<TenantResponse> result = tenantService.listTenants(pageable);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("should return all tenants when they exist")
    void shouldReturnAllTenants() {
      // Given
      createTestTenant("Tenant One", "tenant-one");
      createTestTenant("Tenant Two", "tenant-two");
      createTestTenant("Tenant Three", "tenant-three");

      Pageable pageable = PageRequest.of(0, 10);

      // When
      Page<TenantResponse> result = tenantService.listTenants(pageable);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(3);
      assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("should return paginated results")
    void shouldReturnPaginatedResults() {
      // Given
      createTestTenant("Tenant One", "tenant-1");
      createTestTenant("Tenant Two", "tenant-2");
      createTestTenant("Tenant Three", "tenant-3");
      createTestTenant("Tenant Four", "tenant-4");
      createTestTenant("Tenant Five", "tenant-5");

      Pageable pageable = PageRequest.of(0, 2);

      // When
      Page<TenantResponse> result = tenantService.listTenants(pageable);

      // Then
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getTotalElements()).isEqualTo(5);
      assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("should return second page of results")
    void shouldReturnSecondPage() {
      // Given
      createTestTenant("Tenant One", "tenant-1");
      createTestTenant("Tenant Two", "tenant-2");
      createTestTenant("Tenant Three", "tenant-3");

      Pageable pageable = PageRequest.of(1, 2);

      // When
      Page<TenantResponse> result = tenantService.listTenants(pageable);

      // Then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getNumber()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("getTenantById()")
  class GetTenantById {
    @Test
    @DisplayName("should return tenant when it exists")
    void shouldReturnExistingTenant() {
      // Given
      Long tenantId = createTestTenant("Test Company", "test-company");

      // When
      TenantResponse result = tenantService.getTenantById(tenantId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(tenantId);
      assertThat(result.name()).isEqualTo("Test Company");
      assertThat(result.slug()).isEqualTo("test-company");
      assertThat(result.status()).isEqualTo(TenantStatus.ACTIVE);
      assertThat(result.type()).isEqualTo(TenantType.STANDARD);
      assertThat(result.ownerId()).isEqualTo(testUser.id());
    }

    @Test
    @DisplayName("should throw EntityNotFoundException when tenant does not exist")
    void shouldThrowExceptionWhenTenantNotFound() {
      // Given
      Long nonExistentId = 99999L;

      // When & Then
      assertThatThrownBy(() -> tenantService.getTenantById(nonExistentId))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Tenant with ID '99999' not found");
    }
  }

  @Nested
  @DisplayName("getTenantBySlug()")
  class GetTenantBySlug {
    @Test
    @DisplayName("should return tenant when slug exists")
    void shouldReturnTenantBySlug() {
      // Given
      createTestTenant("Test Company", "test-company");

      // When
      TenantResponse result = tenantService.getTenantBySlug("test-company");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.slug()).isEqualTo("test-company");
      assertThat(result.name()).isEqualTo("Test Company");
    }

    @Test
    @DisplayName("should throw EntityNotFoundException when slug does not exist")
    void shouldThrowExceptionWhenSlugNotFound() {
      // When & Then
      assertThatThrownBy(() -> tenantService.getTenantBySlug("non-existent-slug"))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Tenant with slug 'non-existent-slug' not found");
    }

    @Test
    @DisplayName("should differentiate between similar slugs")
    void shouldDifferentiateBetweenSimilarSlugs() {
      // Given
      createTestTenant("Test Company", "test-company");
      createTestTenant("Test Company 2", "test-company-2");

      // When
      TenantResponse result = tenantService.getTenantBySlug("test-company-2");

      // Then
      assertThat(result.slug()).isEqualTo("test-company-2");
      assertThat(result.name()).isEqualTo("Test Company 2");
    }
  }

  @Nested
  @DisplayName("updateTenantById()")
  class UpdateTenantById {
    @Test
    @DisplayName("should update tenant name successfully")
    void shouldUpdateTenantName() {
      // Given
      Long tenantId = createTestTenant("Original Name", "test-slug");
      TenantUpdateRequest request = new TenantUpdateRequest("Updated Name");

      // When
      TenantResponse result = tenantService.updateTenantById(tenantId, request);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(tenantId);
      assertThat(result.name()).isEqualTo("Updated Name");
      assertThat(result.slug()).isEqualTo("test-slug");

      // Verify persistence
      Tenant updated = tenantRepository.findById(tenantId).orElseThrow();
      assertThat(updated.name()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("should throw EntityNotFoundException when tenant does not exist")
    void shouldThrowExceptionWhenUpdatingNonExistentTenant() {
      // Given
      TenantUpdateRequest request = new TenantUpdateRequest("Updated Name");

      // When & Then
      assertThatThrownBy(() -> tenantService.updateTenantById(99999L, request))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Tenant with ID '99999' not found");
    }

    @Test
    @DisplayName("should preserve other fields when updating name")
    void shouldPreserveOtherFieldsWhenUpdating() {
      // Given
      Long tenantId = createTestTenant("Original Name", "original-slug");
      Tenant original = tenantRepository.findById(tenantId).orElseThrow();
      TenantUpdateRequest request = new TenantUpdateRequest("New Name");

      // When
      tenantService.updateTenantById(tenantId, request);

      // Then
      Tenant updated = tenantRepository.findById(tenantId).orElseThrow();
      assertThat(updated.slug()).isEqualTo(original.slug());
      assertThat(updated.type()).isEqualTo(original.type());
      assertThat(updated.status()).isEqualTo(original.status());
      assertThat(updated.ownerId()).isEqualTo(original.ownerId());
      assertThat(updated.createdAt()).isEqualTo(original.createdAt());
    }
  }

  @Nested
  @DisplayName("deleteTenantById()")
  class DeleteTenantById {
    @Test
    @DisplayName("should delete existing tenant successfully")
    void shouldDeleteExistingTenant() {
      // Given
      Long tenantId = createTestTenant("Test Company", "test-company");
      assertThat(tenantRepository.existsById(tenantId)).isTrue();

      // When
      tenantService.deleteTenantById(tenantId);

      // Then
      assertThat(tenantRepository.existsById(tenantId)).isFalse();
      assertThat(tenantRepository.findById(tenantId)).isEmpty();
    }

    @Test
    @DisplayName("should throw EntityNotFoundException when tenant does not exist")
    void shouldThrowExceptionWhenDeletingNonExistentTenant() {
      // When & Then
      assertThatThrownBy(() -> tenantService.deleteTenantById(99999L))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Tenant with ID '99999' not found");
    }

    @Test
    @DisplayName("should not affect other tenants when deleting one")
    void shouldNotAffectOtherTenantsWhenDeletingOne() {
      // Given
      Long tenant1Id = createTestTenant("Tenant One", "tenant-1");
      Long tenant2Id = createTestTenant("Tenant Two", "tenant-2");
      Long tenant3Id = createTestTenant("Tenant Three", "tenant-3");

      // When
      tenantService.deleteTenantById(tenant2Id);

      // Then
      assertThat(tenantRepository.existsById(tenant1Id)).isTrue();
      assertThat(tenantRepository.existsById(tenant2Id)).isFalse();
      assertThat(tenantRepository.existsById(tenant3Id)).isTrue();
      assertThat(tenantRepository.count()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("existsBySlug()")
  class ExistsBySlug {
    @Test
    @DisplayName("should return true when slug exists")
    void shouldReturnTrueWhenSlugExists() {
      // Given
      createTestTenant("Test Company", "test-company");

      // When
      boolean exists = tenantService.existsBySlug("test-company");

      // Then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should return false when slug does not exist")
    void shouldReturnFalseWhenSlugDoesNotExist() {
      // When
      boolean exists = tenantService.existsBySlug("non-existent-slug");

      // Then
      assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("should return true for each existing slug")
    void shouldReturnTrueForEachExistingSlug() {
      // Given
      createTestTenant("Company One", "company-one");
      createTestTenant("Company Two", "company-two");

      // When & Then
      assertThat(tenantService.existsBySlug("company-one")).isTrue();
      assertThat(tenantService.existsBySlug("company-two")).isTrue();
      assertThat(tenantService.existsBySlug("company-three")).isFalse();
    }
  }

  @Nested
  @DisplayName("Complex Scenarios")
  class ComplexScenarios {
    @Test
    @DisplayName("should handle full tenant lifecycle")
    void shouldHandleFullTenantLifecycle() {
      // Create
      TenantCreateRequest createRequest = new TenantCreateRequest("Lifecycle Test", "lifecycle");
      Long tenantId = tenantService.createTenant(createRequest, testUser);
      assertThat(tenantId).isNotNull();

      // Read by ID
      TenantResponse tenant = tenantService.getTenantById(tenantId);
      assertThat(tenant.name()).isEqualTo("Lifecycle Test");

      // Read by slug
      TenantResponse tenantBySlug = tenantService.getTenantBySlug("lifecycle");
      assertThat(tenantBySlug.id()).isEqualTo(tenantId);

      // Update
      TenantUpdateRequest updateRequest = new TenantUpdateRequest("Updated Lifecycle");
      TenantResponse updated = tenantService.updateTenantById(tenantId, updateRequest);
      assertThat(updated.name()).isEqualTo("Updated Lifecycle");

      // Verify existence
      assertThat(tenantService.existsBySlug("lifecycle")).isTrue();

      // Delete
      tenantService.deleteTenantById(tenantId);
      assertThat(tenantService.existsBySlug("lifecycle")).isFalse();
      assertThatThrownBy(() -> tenantService.getTenantById(tenantId))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("should maintain data consistency across multiple operations")
    void shouldMaintainDataConsistencyAcrossOperations() {
      // Given - Create multiple tenants
      Long id1 = createTestTenant("Tenant Alpha", "alpha");
      Long id2 = createTestTenant("Tenant Beta", "beta");
      Long id3 = createTestTenant("Tenant Gamma", "gamma");

      // When - Update one
      tenantService.updateTenantById(id2, new TenantUpdateRequest("Beta Updated"));

      // Then - Verify all tenants are independent
      TenantResponse tenant1 = tenantService.getTenantById(id1);
      TenantResponse tenant2 = tenantService.getTenantById(id2);
      TenantResponse tenant3 = tenantService.getTenantById(id3);

      assertThat(tenant1.name()).isEqualTo("Tenant Alpha");
      assertThat(tenant2.name()).isEqualTo("Beta Updated");
      assertThat(tenant3.name()).isEqualTo("Tenant Gamma");

      // When - Delete one
      tenantService.deleteTenantById(id1);

      // Then - Others remain intact
      assertThat(tenantRepository.existsById(id1)).isFalse();
      assertThat(tenantRepository.existsById(id2)).isTrue();
      assertThat(tenantRepository.existsById(id3)).isTrue();
    }
  }

  // Helper methods
  private Long createTestTenant(String name, String slug) {
    TenantCreateRequest request = new TenantCreateRequest(name, slug);
    return tenantService.createTenant(request, testUser);
  }
}
