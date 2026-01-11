package com.turtleby.idms.web.tenant.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.turtleby.idms.web.TestcontainersConfiguration;
import com.turtleby.idms.web.security.userdetails.SecurityUser;
import com.turtleby.idms.web.tenant.dto.TenantCreateRequest;
import com.turtleby.idms.web.tenant.dto.TenantUpdateRequest;
import com.turtleby.idms.web.user.entity.User;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("TenantController Integration Tests")
class TenantControllerITests {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JdbcTemplate jdbcTemplate;

  private SecurityUser testSecurityUser;
  private String testUserId;

  @BeforeEach
  void setUp() {
    // Create test user
    testUserId = UUID.randomUUID().toString();
    jdbcTemplate.update(
        "INSERT INTO users (id, username, password, is_active) VALUES (?, ?, ?, ?)",
        testUserId,
        "testuser",
        "{bcrypt}$2a$10$password",
        true);

    User testUser = new User(testUserId, "testuser", "{bcrypt}$2a$10$password", true);
    testSecurityUser = new SecurityUser(testUser);
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.execute("DELETE FROM tenant");
    jdbcTemplate.execute("DELETE FROM users");
  }

  @Nested
  @DisplayName("POST /api/v1/tenants")
  class CreateTenant {
    @Test
    @DisplayName("should create tenant with valid data")
    void shouldCreateTenantSuccessfully() throws Exception {
      // Given
      TenantCreateRequest request = new TenantCreateRequest("Integration Test Tenant", "int-test");

      // When & Then
      MvcResult result =
          mockMvc
              .perform(
                  post("/api/v1/tenants")
                      .with(user(testSecurityUser))
                      .with(csrf())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isCreated())
              .andExpect(header().exists("Location"))
              .andReturn();

      // Verify location header contains tenant ID
      String location = result.getResponse().getHeader("Location");
      assert location != null && location.matches(".*/api/v1/tenants/\\d+");
    }

    @Test
    @DisplayName("should return 400 for invalid request")
    void shouldReturnBadRequestForInvalidData() throws Exception {
      // Given - blank name
      TenantCreateRequest request = new TenantCreateRequest("", "test-slug");

      // When & Then
      mockMvc
          .perform(
              post("/api/v1/tenants")
                  .with(user(testSecurityUser))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturnUnauthorizedWithoutAuth() throws Exception {
      // Given
      TenantCreateRequest request = new TenantCreateRequest("Test Tenant", "test");

      // When & Then
      mockMvc
          .perform(
              post("/api/v1/tenants")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/tenants")
  class ListTenants {
    @Test
    @DisplayName("should return empty list when no tenants exist")
    void shouldReturnEmptyList() throws Exception {
      mockMvc
          .perform(get("/api/v1/tenants").with(user(testSecurityUser)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content", hasSize(0)))
          .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("should return list of tenants")
    void shouldReturnListOfTenants() throws Exception {
      // Given - create test tenants
      createTenant("Tenant One", "tenant-one");
      createTenant("Tenant Two", "tenant-two");
      createTenant("Tenant Three", "tenant-three");

      // When & Then
      mockMvc
          .perform(get("/api/v1/tenants").with(user(testSecurityUser)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(3)))
          .andExpect(jsonPath("$.totalElements").value(3))
          .andExpect(jsonPath("$.content[0].name").exists())
          .andExpect(jsonPath("$.content[0].slug").exists());
    }

    @Test
    @DisplayName("should support pagination")
    void shouldSupportPagination() throws Exception {
      // Given - create 5 tenants
      for (int i = 1; i <= 5; i++) {
        createTenant("Tenant " + i, "tenant-" + i);
      }

      // When & Then - request page with size 2
      mockMvc
          .perform(
              get("/api/v1/tenants")
                  .with(user(testSecurityUser))
                  .param("page", "0")
                  .param("size", "2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)))
          .andExpect(jsonPath("$.totalElements").value(5))
          .andExpect(jsonPath("$.totalPages").value(3))
          .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("should return second page correctly")
    void shouldReturnSecondPage() throws Exception {
      // Given
      for (int i = 1; i <= 5; i++) {
        createTenant("Tenant " + i, "tenant-" + i);
      }

      // When & Then
      mockMvc
          .perform(
              get("/api/v1/tenants")
                  .with(user(testSecurityUser))
                  .param("page", "1")
                  .param("size", "2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)))
          .andExpect(jsonPath("$.number").value(1));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/tenants?slug={slug}")
  class GetTenantBySlug {
    @Test
    @DisplayName("should return tenant when slug exists")
    void shouldReturnTenantBySlug() throws Exception {
      // Given
      createTenant("Test Company", "test-company");

      // When & Then
      mockMvc
          .perform(
              get("/api/v1/tenants").with(user(testSecurityUser)).param("slug", "test-company"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.name").value("Test Company"))
          .andExpect(jsonPath("$.slug").value("test-company"))
          .andExpect(jsonPath("$.type").value("STANDARD"))
          .andExpect(jsonPath("$.status").value("ACTIVE"))
          .andExpect(jsonPath("$.ownerId").value(testUserId));
    }

    @Test
    @DisplayName("should return 404 when slug not found")
    void shouldReturnNotFoundForNonExistentSlug() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/tenants").with(user(testSecurityUser)).param("slug", "non-existent"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/tenants/{tenantId}")
  class GetTenantById {
    @Test
    @DisplayName("should return tenant when id exists")
    void shouldReturnTenantById() throws Exception {
      // Given
      Long tenantId = createTenant("Test Tenant", "test-tenant");

      // When & Then
      mockMvc
          .perform(get("/api/v1/tenants/" + tenantId).with(user(testSecurityUser)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(tenantId))
          .andExpect(jsonPath("$.name").value("Test Tenant"))
          .andExpect(jsonPath("$.slug").value("test-tenant"))
          .andExpect(jsonPath("$.createdAt").exists())
          .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("should return 404 when id not found")
    void shouldReturnNotFoundForNonExistentId() throws Exception {
      mockMvc
          .perform(get("/api/v1/tenants/99999").with(user(testSecurityUser)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/tenants/{tenantId}")
  class UpdateTenantById {
    @Test
    @DisplayName("should update tenant successfully")
    void shouldUpdateTenantSuccessfully() throws Exception {
      // Given
      Long tenantId = createTenant("Original Name", "original-slug");
      TenantUpdateRequest request = new TenantUpdateRequest("Updated Name");

      // When & Then
      mockMvc
          .perform(
              put("/api/v1/tenants/" + tenantId)
                  .with(user(testSecurityUser))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(tenantId))
          .andExpect(jsonPath("$.name").value("Updated Name"))
          .andExpect(jsonPath("$.slug").value("original-slug")); // Slug unchanged
    }

    @Test
    @DisplayName("should return 400 for invalid request")
    void shouldReturnBadRequestForInvalidData() throws Exception {
      // Given
      Long tenantId = createTenant("Test", "test");
      TenantUpdateRequest request = new TenantUpdateRequest(""); // Blank name

      // When & Then
      mockMvc
          .perform(
              put("/api/v1/tenants/" + tenantId)
                  .with(user(testSecurityUser))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 404 when tenant not found")
    void shouldReturnNotFoundForNonExistentTenant() throws Exception {
      // Given
      TenantUpdateRequest request = new TenantUpdateRequest("Updated Name");

      // When & Then
      mockMvc
          .perform(
              put("/api/v1/tenants/99999")
                  .with(user(testSecurityUser))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/tenants/{tenantId}")
  class DeleteTenantById {
    @Test
    @DisplayName("should delete tenant successfully")
    void shouldDeleteTenantSuccessfully() throws Exception {
      // Given
      Long tenantId = createTenant("To Delete", "to-delete");

      // When & Then
      mockMvc
          .perform(delete("/api/v1/tenants/" + tenantId).with(user(testSecurityUser)).with(csrf()))
          .andExpect(status().isNoContent());

      // Verify tenant is deleted
      mockMvc
          .perform(get("/api/v1/tenants/" + tenantId).with(user(testSecurityUser)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 404 when tenant not found")
    void shouldReturnNotFoundForNonExistentTenant() throws Exception {
      mockMvc
          .perform(delete("/api/v1/tenants/99999").with(user(testSecurityUser)).with(csrf()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should not affect other tenants")
    void shouldNotAffectOtherTenants() throws Exception {
      // Given
      Long tenant1 = createTenant("Tenant 1", "tenant-1");
      Long tenant2 = createTenant("Tenant 2", "tenant-2");

      // When - delete tenant1
      mockMvc
          .perform(delete("/api/v1/tenants/" + tenant1).with(user(testSecurityUser)).with(csrf()))
          .andExpect(status().isNoContent());

      // Then - tenant2 should still exist
      mockMvc
          .perform(get("/api/v1/tenants/" + tenant2).with(user(testSecurityUser)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(tenant2));
    }
  }

  @Nested
  @DisplayName("End-to-End Scenarios")
  class EndToEndScenarios {
    @Test
    @DisplayName("should handle complete CRUD lifecycle")
    void shouldHandleCompleteCrudLifecycle() throws Exception {
      // Create
      TenantCreateRequest createRequest = new TenantCreateRequest("Lifecycle Test", "lifecycle");
      MvcResult createResult =
          mockMvc
              .perform(
                  post("/api/v1/tenants")
                      .with(user(testSecurityUser))
                      .with(csrf())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(createRequest)))
              .andExpect(status().isCreated())
              .andReturn();

      String location = createResult.getResponse().getHeader("Location");
      assert location != null;
      String tenantId = location.substring(location.lastIndexOf("/") + 1);

      // Read
      mockMvc
          .perform(get("/api/v1/tenants/" + tenantId).with(user(testSecurityUser)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Lifecycle Test"));

      // Update
      TenantUpdateRequest updateRequest = new TenantUpdateRequest("Updated Lifecycle");
      mockMvc
          .perform(
              put("/api/v1/tenants/" + tenantId)
                  .with(user(testSecurityUser))
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Updated Lifecycle"));

      // Delete
      mockMvc
          .perform(delete("/api/v1/tenants/" + tenantId).with(user(testSecurityUser)).with(csrf()))
          .andExpect(status().isNoContent());

      // Verify deleted
      mockMvc
          .perform(get("/api/v1/tenants/" + tenantId).with(user(testSecurityUser)))
          .andExpect(status().isNotFound());
    }
  }

  // Helper method
  private Long createTenant(String name, String slug) {
    return jdbcTemplate.queryForObject(
        "INSERT INTO tenant (name, slug, type, status, owner_id, created_at, updated_at) "
            + "VALUES (?, ?, 'STANDARD', 'ACTIVE', ?, NOW(), NOW()) RETURNING id",
        Long.class,
        name,
        slug,
        testUserId);
  }
}
