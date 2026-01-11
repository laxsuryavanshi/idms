package com.turtleby.idms.web.tenant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.turtleby.idms.web.common.exception.EntityNotFoundException;
import com.turtleby.idms.web.security.userdetails.SecurityUser;
import com.turtleby.idms.web.tenant.dto.TenantCreateRequest;
import com.turtleby.idms.web.tenant.dto.TenantResponse;
import com.turtleby.idms.web.tenant.dto.TenantUpdateRequest;
import com.turtleby.idms.web.tenant.entity.TenantStatus;
import com.turtleby.idms.web.tenant.entity.TenantType;
import com.turtleby.idms.web.tenant.service.TenantManager;
import com.turtleby.idms.web.user.entity.User;

@WebMvcTest(TenantController.class)
@DisplayName("TenantController Unit Tests")
class TenantControllerTests {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private TenantManager tenantManager;

  private TenantResponse testTenantResponse;
  private SecurityUser testSecurityUser;

  @BeforeEach
  void setUp() {
    // Create a test User and SecurityUser for authentication
    User testUser = new User("test-user-id", "testuser", "password", true);
    testSecurityUser = new SecurityUser(testUser);

    testTenantResponse =
        new TenantResponse(
            1L,
            "Test Tenant",
            "test-tenant",
            TenantType.STANDARD,
            TenantStatus.ACTIVE,
            "user-123",
            Instant.now(),
            Instant.now());
  }

  @Nested
  @DisplayName("POST /api/v1/tenants")
  class CreateTenant {
    @Test
    @DisplayName("should create tenant and return 201 with location header")
    void shouldCreateTenantSuccessfully() throws Exception {
      // Given
      TenantCreateRequest request = new TenantCreateRequest("New Tenant", "new-tenant");
      when(tenantManager.createTenant(any(TenantCreateRequest.class), any(User.class)))
          .thenReturn(1L);

      // When & Then
      mockMvc
          .perform(
              post("/api/v1/tenants")
                  .with(csrf())
                  .with(user(testSecurityUser))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "/api/v1/tenants/1"));

      verify(tenantManager).createTenant(any(TenantCreateRequest.class), any(User.class));
    }

    @Test
    @WithMockUser
    @DisplayName("should return 400 when name is blank")
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
      // Given
      TenantCreateRequest request = new TenantCreateRequest("", "test-slug");

      // When & Then
      mockMvc
          .perform(
              post("/api/v1/tenants")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/tenants")
  class ListTenants {
    @Test
    @WithMockUser
    @DisplayName("should return paginated list of tenants")
    void shouldReturnPaginatedTenants() throws Exception {
      // Given
      List<TenantResponse> tenants = List.of(testTenantResponse);
      Page<TenantResponse> page = new PageImpl<>(tenants, PageRequest.of(0, 50), 1);
      when(tenantManager.listTenants(any(Pageable.class))).thenReturn(page);

      // When & Then
      mockMvc
          .perform(get("/api/v1/tenants"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content[0].id").value(1))
          .andExpect(jsonPath("$.content[0].name").value("Test Tenant"))
          .andExpect(jsonPath("$.content[0].slug").value("test-tenant"))
          .andExpect(jsonPath("$.totalElements").value(1))
          .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    @WithMockUser
    @DisplayName("should support custom page size")
    void shouldSupportCustomPageSize() throws Exception {
      // Given
      Page<TenantResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
      when(tenantManager.listTenants(any(Pageable.class))).thenReturn(page);

      // When & Then
      mockMvc
          .perform(get("/api/v1/tenants").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @WithMockUser
    @DisplayName("should support pagination")
    void shouldSupportPagination() throws Exception {
      // Given
      Page<TenantResponse> page = new PageImpl<>(List.of(), PageRequest.of(2, 20), 100);
      when(tenantManager.listTenants(any(Pageable.class))).thenReturn(page);

      // When & Then
      mockMvc
          .perform(get("/api/v1/tenants").param("page", "2").param("size", "20"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.number").value(2))
          .andExpect(jsonPath("$.totalElements").value(100));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/tenants?slug={slug}")
  class GetTenantBySlug {
    @Test
    @WithMockUser
    @DisplayName("should return tenant when slug exists")
    void shouldReturnTenantBySlug() throws Exception {
      // Given
      when(tenantManager.getTenantBySlug("test-tenant")).thenReturn(testTenantResponse);

      // When & Then
      mockMvc
          .perform(get("/api/v1/tenants").param("slug", "test-tenant"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.name").value("Test Tenant"))
          .andExpect(jsonPath("$.slug").value("test-tenant"))
          .andExpect(jsonPath("$.type").value("STANDARD"))
          .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    @DisplayName("should return 404 when slug not found")
    void shouldReturnNotFoundWhenSlugDoesNotExist() throws Exception {
      // Given
      when(tenantManager.getTenantBySlug("non-existent"))
          .thenThrow(new EntityNotFoundException("Tenant with slug 'non-existent' not found"));

      // When & Then
      mockMvc
          .perform(get("/api/v1/tenants").param("slug", "non-existent"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/tenants/{tenantId}")
  class GetTenantById {
    @Test
    @WithMockUser
    @DisplayName("should return tenant when id exists")
    void shouldReturnTenantById() throws Exception {
      // Given
      when(tenantManager.getTenantById(1L)).thenReturn(testTenantResponse);

      // When & Then
      mockMvc
          .perform(get("/api/v1/tenants/1"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.name").value("Test Tenant"))
          .andExpect(jsonPath("$.slug").value("test-tenant"));
    }

    @Test
    @WithMockUser
    @DisplayName("should return 404 when id not found")
    void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
      // Given
      when(tenantManager.getTenantById(999L))
          .thenThrow(new EntityNotFoundException("Tenant with ID '999' not found"));

      // When & Then
      mockMvc.perform(get("/api/v1/tenants/999")).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/tenants/{tenantId}")
  class UpdateTenantById {
    @Test
    @WithMockUser
    @DisplayName("should update tenant and return updated data")
    void shouldUpdateTenantSuccessfully() throws Exception {
      // Given
      TenantUpdateRequest request = new TenantUpdateRequest("Updated Name");
      TenantResponse updatedResponse =
          new TenantResponse(
              1L,
              "Updated Name",
              "test-tenant",
              TenantType.STANDARD,
              TenantStatus.ACTIVE,
              "user-123",
              Instant.now(),
              Instant.now());
      when(tenantManager.updateTenantById(eq(1L), any(TenantUpdateRequest.class)))
          .thenReturn(updatedResponse);

      // When & Then
      mockMvc
          .perform(
              put("/api/v1/tenants/1")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.name").value("Updated Name"));

      verify(tenantManager).updateTenantById(eq(1L), any(TenantUpdateRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("should return 400 when name is blank")
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
      // Given
      TenantUpdateRequest request = new TenantUpdateRequest("");

      // When & Then
      mockMvc
          .perform(
              put("/api/v1/tenants/1")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("should return 404 when tenant not found")
    void shouldReturnNotFoundWhenTenantDoesNotExist() throws Exception {
      // Given
      TenantUpdateRequest request = new TenantUpdateRequest("Updated Name");
      when(tenantManager.updateTenantById(eq(999L), any(TenantUpdateRequest.class)))
          .thenThrow(new EntityNotFoundException("Tenant with ID '999' not found"));

      // When & Then
      mockMvc
          .perform(
              put("/api/v1/tenants/999")
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
    @WithMockUser
    @DisplayName("should delete tenant and return 204")
    void shouldDeleteTenantSuccessfully() throws Exception {
      // When & Then
      mockMvc.perform(delete("/api/v1/tenants/1").with(csrf())).andExpect(status().isNoContent());

      verify(tenantManager).deleteTenantById(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("should return 404 when tenant not found")
    void shouldReturnNotFoundWhenTenantDoesNotExist() throws Exception {
      // Given
      doThrow(new EntityNotFoundException("Tenant with ID '999' not found"))
          .when(tenantManager)
          .deleteTenantById(999L);

      // When & Then
      mockMvc.perform(delete("/api/v1/tenants/999").with(csrf())).andExpect(status().isNotFound());
    }
  }
}
