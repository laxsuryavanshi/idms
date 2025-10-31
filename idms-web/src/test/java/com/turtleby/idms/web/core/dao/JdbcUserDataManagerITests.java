package com.turtleby.idms.web.core.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.turtleby.idms.web.TestcontainersConfiguration;
import com.turtleby.idms.web.core.entity.User;

@Import(TestcontainersConfiguration.class)
@DataJdbcTest
@DisplayName("JdbcUserDataManager Integration Tests")
class JdbcUserDataManagerITests {
  @Autowired private JdbcTemplate jdbcTemplate;

  private JdbcUserDataManager userDataManager;

  @BeforeEach
  void setUp() {
    userDataManager = new JdbcUserDataManager(jdbcTemplate);
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.execute("delete from users");
  }

  @Nested
  @DisplayName("getUserByUsername() - Success Cases")
  class GetUserByUsernameSuccessCases {
    @Test
    @DisplayName("should return user when user exists with valid credentials")
    void shouldReturnUserWhenUserExists() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "john.doe@example.com";
      String password = "{bcrypt}$2a$10$abcdefghijklmnopqrstuvwxyz123456";
      boolean isActive = true;

      insertUser(userId, username, password, isActive);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(userId);
      assertThat(result.username()).isEqualTo(username);
      assertThat(result.password()).isEqualTo(password);
      assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("should return user when user is inactive")
    void shouldReturnInactiveUser() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "inactive.user@example.com";
      String password = "{bcrypt}$2a$10$hashedpassword";
      boolean isActive = false;

      insertUser(userId, username, password, isActive);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.isActive()).isFalse();
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
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.username()).isEqualTo(username);
    }

    @Test
    @DisplayName("should handle very long username (max length)")
    void shouldHandleMaxLengthUsername() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "a".repeat(64) + "@" + "b".repeat(186) + ".com";
      String password = "{bcrypt}$2a$10$hashedpassword";

      insertUser(userId, username, password, true);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.username()).hasSize(255);
    }

    @Test
    @DisplayName("should return null when user does not exist")
    void shouldReturnNullWhenUserNotFound() {
      // When
      User result = userDataManager.getUserByUsername("nonexistent@example.com");

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should handle case-sensitive username lookup")
    void shouldBeCaseSensitive() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "CaseSensitive@Example.com";
      insertUser(userId, username, "{bcrypt}$2a$10$hashedpassword", true);

      // When
      User upperCaseResult = userDataManager.getUserByUsername("CASESENSITIVE@EXAMPLE.COM");
      User lowerCaseResult = userDataManager.getUserByUsername("casesensitive@example.com");
      User exactCaseResult = userDataManager.getUserByUsername(username);

      // Then - PostgreSQL is case-sensitive for varchar comparison by default
      assertThat(upperCaseResult).isNull();
      assertThat(lowerCaseResult).isNull();
      assertThat(exactCaseResult).isNotNull();
    }
  }

  @Nested
  @DisplayName("getUserByUsername() - Error Cases")
  class GetUserByUsernameErrorCases {
    @Test
    @DisplayName("should throw exception when multiple users found with same username")
    void shouldThrowExceptionWhenMultipleUsersFound() {
      // Given - Drop the unique constraint temporarily and insert duplicates
      jdbcTemplate.execute("alter table users drop constraint users_username_key");

      String username = "duplicate@example.com";
      insertUser("id1", username, "pass1", true);
      insertUser("id2", username, "pass2", true);

      // When/Then
      assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class)
          .isThrownBy(() -> userDataManager.getUserByUsername(username))
          .withMessageContaining("Incorrect result size");
    }

    @Test
    @DisplayName("should handle SQL injection attempts safely")
    void shouldPreventSqlInjection() {
      // Given - attempt SQL injection
      String maliciousUsername = "admin' OR '1'='1";

      // When
      User result = userDataManager.getUserByUsername(maliciousUsername);

      // Then - should treat it as a literal string and return null (not found)
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should handle null username gracefully")
    void shouldHandleNullUsername() {
      // When/Then - JdbcTemplate will throw exception for null parameter
      assertThatThrownBy(() -> userDataManager.getUserByUsername(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should handle empty string username")
    void shouldHandleEmptyStringUsername() {
      // When
      User result = userDataManager.getUserByUsername("");

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should handle whitespace-only username")
    void shouldHandleWhitespaceUsername() {
      // When
      User result = userDataManager.getUserByUsername("   ");

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("Custom SQL Query Tests")
  class CustomSqlQueryTests {
    @Test
    @DisplayName("should use custom SQL query when set")
    void shouldUseCustomSqlQuery() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "test@example.com";
      insertUser(userId, username, "password", true);

      // Custom query that adds a WHERE clause
      String customSql =
          "select id, username, password, is_active from users where username = ? and is_active ="
              + " true";
      userDataManager.setGetUserSql(customSql);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("should filter out inactive users with custom query")
    void shouldFilterInactiveUsersWithCustomQuery() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "inactive@example.com";
      insertUser(userId, username, "password", false);

      String customSql =
          "select id, username, password, is_active from users where username = ? and is_active ="
              + " true";
      userDataManager.setGetUserSql(customSql);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should throw exception when custom SQL is null")
    void shouldThrowExceptionWhenCustomSqlIsNull() {
      // When/Then
      assertThatThrownBy(() -> userDataManager.setGetUserSql(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("getUserSql must not be null");
    }
  }

  @Nested
  @DisplayName("Custom RowMapper Tests")
  class CustomRowMapperTests {
    @Test
    @DisplayName("should use custom row mapper when set")
    void shouldUseCustomRowMapper() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "test@example.com";
      insertUser(userId, username, "password", true);

      // Custom row mapper that sets a different username
      RowMapper<User> customMapper =
          (rs, rowNum) ->
              new User(
                  rs.getString("id"),
                  "CUSTOM_" + rs.getString("username"),
                  rs.getString("password"),
                  rs.getBoolean("is_active"));

      userDataManager.setRowMapper(customMapper);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.username()).startsWith("CUSTOM_");
    }

    @Test
    @DisplayName("should throw exception when custom row mapper is null")
    void shouldThrowExceptionWhenCustomRowMapperIsNull() {
      // When/Then
      assertThatThrownBy(() -> userDataManager.setRowMapper(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("rowMapper must not be null");
    }

    @Test
    @DisplayName("should handle row mapper that returns null")
    void shouldHandleRowMapperReturningNull() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "test@example.com";
      insertUser(userId, username, "password", true);

      RowMapper<User> nullReturningMapper = (rs, rowNum) -> null;
      userDataManager.setRowMapper(nullReturningMapper);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("Edge Cases and Boundary Tests")
  class EdgeCasesAndBoundaryTests {
    @Test
    @DisplayName("should handle password with maximum length")
    void shouldHandleMaxLengthPassword() {
      // Given - BCrypt hashes are 60 chars, but column is varchar(255)
      String userId = UUID.randomUUID().toString();
      String username = "test@example.com";
      String password = "a".repeat(255); // Max length

      insertUser(userId, username, password, true);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.password()).hasSize(255);
    }

    @Test
    @DisplayName("should handle unicode characters in username")
    void shouldHandleUnicodeCharacters() {
      // Given
      String userId = UUID.randomUUID().toString();
      String username = "user.日本語@example.com";
      String password = "password";

      insertUser(userId, username, password, true);

      // When
      User result = userDataManager.getUserByUsername(username);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.username()).isEqualTo(username);
    }
  }

  // Helper methods
  private void insertUser(String id, String username, String password, boolean isActive) {
    jdbcTemplate.update(
        "insert into users (id, username, password, is_active) values (?, ?, ?, ?)",
        id,
        username,
        password,
        isActive);
  }
}
