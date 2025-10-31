package com.turtleby.idms.web.security.userdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.turtleby.idms.web.TestcontainersConfiguration;
import com.turtleby.idms.web.core.dao.JdbcUserDataManager;
import com.turtleby.idms.web.core.dao.UserDataManager;

@Import(TestcontainersConfiguration.class)
@DataJdbcTest
@DisplayName("UserDetailsManager Integration Tests")
class UserDetailsManagerITests {
  @Autowired private JdbcTemplate jdbcTemplate;

  private UserDataManager userDataManager;
  private UserDetailsManager userDetailsManager;

  @BeforeEach
  void setUp() {
    userDataManager = new JdbcUserDataManager(jdbcTemplate);
    userDetailsManager = new UserDetailsManager(userDataManager);
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.execute("delete from users");
  }

  @Nested
  @DisplayName("loadUserByUsername() - Success Cases")
  class LoadUserByUsernameSuccessCases {
    @Test
    @DisplayName("should load active user and return UserDetails")
    void shouldLoadActiveUser() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "john.doe@example.com";
      String password = "{bcrypt}$2a$10$hashedpassword";
      boolean isActive = true;

      insertUser(userId, username, password, isActive);

      // When
      UserDetails result = userDetailsManager.loadUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isInstanceOf(SecurityUser.class);
      assertThat(result.getUsername()).isEqualTo(username);
      assertThat(result.getPassword()).isEqualTo(password);
      assertThat(result.isEnabled()).isTrue();
      assertThat(result.isAccountNonExpired()).isTrue();
      assertThat(result.isAccountNonLocked()).isTrue();
      assertThat(result.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("should load inactive user and return disabled UserDetails")
    void shouldLoadInactiveUser() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "inactive.user@example.com";
      String password = "{bcrypt}$2a$10$hashedpassword";
      boolean isActive = false;

      insertUser(userId, username, password, isActive);

      // When
      UserDetails result = userDetailsManager.loadUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.isEnabled()).isFalse();
      assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("should handle username with special characters")
    void shouldHandleSpecialCharactersInUsername() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "user+tag@example.com";
      String password = "{bcrypt}$2a$10$hashedpassword";

      insertUser(userId, username, password, true);

      // When
      UserDetails result = userDetailsManager.loadUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("should return empty authorities collection")
    void shouldReturnEmptyAuthorities() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "user@example.com";
      String password = "{bcrypt}$2a$10$hashedpassword";

      insertUser(userId, username, password, true);

      // When
      UserDetails result = userDetailsManager.loadUserByUsername(username);

      // Then
      assertThat(result.getAuthorities()).isNotNull();
      assertThat(result.getAuthorities()).isEmpty();
    }
  }

  @Nested
  @DisplayName("loadUserByUsername() - Error Cases")
  class LoadUserByUsernameErrorCases {
    @Test
    @DisplayName("should throw UsernameNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      String nonExistentUsername = "nonexistent@example.com";

      // When/Then
      assertThatExceptionOfType(UsernameNotFoundException.class)
          .isThrownBy(() -> userDetailsManager.loadUserByUsername(nonExistentUsername))
          .withMessageContaining("User with username")
          .withMessageContaining(nonExistentUsername)
          .withMessageContaining("not found");
    }

    @Test
    @DisplayName("should throw exception for null username")
    void shouldThrowExceptionForNullUsername() {
      // When/Then - underlying DAO throws NullPointerException for null username
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> userDetailsManager.loadUserByUsername(null))
          .withMessageContaining("username must not be null");
    }

    @Test
    @DisplayName("should throw UsernameNotFoundException for empty username")
    void shouldThrowExceptionForEmptyUsername() {
      // When/Then
      assertThatExceptionOfType(UsernameNotFoundException.class)
          .isThrownBy(() -> userDetailsManager.loadUserByUsername(""))
          .withMessageContaining("not found");
    }
  }

  @Nested
  @DisplayName("Edge Cases and Boundary Tests")
  class EdgeCasesAndBoundaryTests {
    @Test
    @DisplayName("should handle username at maximum length")
    void shouldHandleMaxLengthUsername() {
      // Given - Maximum username length: varchar(255)
      String maxLengthUsername = "a".repeat(255);
      String userId = UUID.randomUUID().toString();

      insertUser(userId, maxLengthUsername, "{bcrypt}$2a$10$hash", true);

      // When
      UserDetails result = userDetailsManager.loadUserByUsername(maxLengthUsername);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).hasSize(255);
    }

    @Test
    @DisplayName("should handle minimum valid username")
    void shouldHandleMinimumUsername() {
      // Given - Minimum valid email: a@b.c (5 characters)
      String minUsername = "a@b.c";
      String userId = UUID.randomUUID().toString();

      insertUser(userId, minUsername, "{bcrypt}$2a$10$hash", true);

      // When
      UserDetails result = userDetailsManager.loadUserByUsername(minUsername);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo(minUsername);
    }

    @Test
    @DisplayName("should handle whitespace in username")
    void shouldHandleWhitespaceInUsername() {
      // Given
      String username = " user@example.com ";
      String userId = UUID.randomUUID().toString();

      insertUser(userId, username, "{bcrypt}$2a$10$hash", true);

      // When
      UserDetails result = userDetailsManager.loadUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo(username);
    }
  }

  @Nested
  @DisplayName("Integration with UserDataManager")
  class IntegrationWithUserDataManager {
    @Test
    @DisplayName("should correctly integrate with UserDataManager for multiple users")
    void shouldHandleMultipleUsers() {
      // Given
      insertUser(UUID.randomUUID().toString(), "user1@example.com", "{bcrypt}$2a$10$hash1", true);
      insertUser(UUID.randomUUID().toString(), "user2@example.com", "{bcrypt}$2a$10$hash2", true);
      insertUser(UUID.randomUUID().toString(), "user3@example.com", "{bcrypt}$2a$10$hash3", false);

      // When
      UserDetails user1 = userDetailsManager.loadUserByUsername("user1@example.com");
      UserDetails user2 = userDetailsManager.loadUserByUsername("user2@example.com");
      UserDetails user3 = userDetailsManager.loadUserByUsername("user3@example.com");

      // Then
      assertThat(user1.isEnabled()).isTrue();
      assertThat(user2.isEnabled()).isTrue();
      assertThat(user3.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("should maintain consistency across multiple loads of same user")
    void shouldMaintainConsistencyAcrossMultipleLoads() {
      // Given
      String username = "consistent.user@example.com";
      String password = "{bcrypt}$2a$10$consistenthash";
      insertUser(UUID.randomUUID().toString(), username, password, true);

      // When
      UserDetails load1 = userDetailsManager.loadUserByUsername(username);
      UserDetails load2 = userDetailsManager.loadUserByUsername(username);
      UserDetails load3 = userDetailsManager.loadUserByUsername(username);

      // Then
      assertThat(load1.getUsername()).isEqualTo(load2.getUsername()).isEqualTo(load3.getUsername());
      assertThat(load1.getPassword()).isEqualTo(load2.getPassword()).isEqualTo(load3.getPassword());
      assertThat(load1.isEnabled()).isEqualTo(load2.isEnabled()).isEqualTo(load3.isEnabled());
    }
  }

  // Helper method to insert test users
  private void insertUser(String id, String username, String password, boolean isActive) {
    jdbcTemplate.update(
        "insert into users (id, username, password, is_active) values (?, ?, ?, ?)",
        id,
        username,
        password,
        isActive);
  }
}
