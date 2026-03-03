package com.turtleby.idms.web.user.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.turtleby.idms.web.TestcontainersConfiguration;
import com.turtleby.idms.web.user.entity.User;

@Import(TestcontainersConfiguration.class)
@DataJdbcTest
@DisplayName("EmailAddressRepository.findUserByEmail() Integration Tests")
class EmailAddressRepositoryITests {
  @Autowired private EmailAddressRepository emailAddressRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @AfterEach
  void tearDown() {
    jdbcTemplate.execute("delete from user_email_address");
    jdbcTemplate.execute("delete from users");
  }

  @Nested
  @DisplayName("findUserByEmail() - Success Cases")
  class FindUserByEmailSuccessCases {
    @Test
    @DisplayName("should return user when email exists")
    void shouldReturnUserWhenEmailExists() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String username = "john.doe";
      final String password = "{bcrypt}$2a$10$hashedpassword";
      final String email = "john.doe@example.com";

      insertUser(userId, username, password, true);
      insertEmailAddress(userId, email, true, true);

      // When
      final Optional<User> result = emailAddressRepository.findUserByEmail(email);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id()).isEqualTo(userId);
      assertThat(result.get().username()).isEqualTo(username);
      assertThat(result.get().password()).isEqualTo(password);
      assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("should return user with primary email when multiple emails exist")
    void shouldReturnUserWithPrimaryEmail() {
      // Given
      final String user1Id = UUID.randomUUID().toString();
      final String user2Id = UUID.randomUUID().toString();
      final String sharedEmail = "shared@example.com";

      // User 1 has the email as non-primary
      insertUser(user1Id, "user1", "{bcrypt}$2a$10$hash1", true);
      insertEmailAddress(user1Id, sharedEmail, false, true);

      // User 2 has the email as primary
      insertUser(user2Id, "user2", "{bcrypt}$2a$10$hash2", true);
      insertEmailAddress(user2Id, sharedEmail, true, true);

      // When
      final Optional<User> result = emailAddressRepository.findUserByEmail(sharedEmail);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id())
          .isEqualTo(user2Id)
          .as("Should return the user with primary email");
      assertThat(result.get().username()).isEqualTo("user2");
    }

    @Test
    @DisplayName(
        "should return user with alphabetically first username when multiple emails with same"
            + " priority")
    void shouldReturnUserWithAlphabeticallyFirstUsername() {
      // Given
      final String user1Id = UUID.randomUUID().toString();
      final String user2Id = UUID.randomUUID().toString();
      final String sharedEmail = "shared@example.com";

      // Both users have the email as non-primary (same priority)
      insertUser(user1Id, "zebra", "{bcrypt}$2a$10$hash1", true);
      insertEmailAddress(user1Id, sharedEmail, false, true);

      insertUser(user2Id, "alpha", "{bcrypt}$2a$10$hash2", true);
      insertEmailAddress(user2Id, sharedEmail, false, true);

      // When
      final Optional<User> result = emailAddressRepository.findUserByEmail(sharedEmail);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().username())
          .isEqualTo("alpha")
          .as("Should return user with alphabetically first username");
    }

    @Test
    @DisplayName("should handle email with special characters")
    void shouldHandleEmailWithSpecialCharacters() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String email = "user+tag@example.com";

      insertUser(userId, "special.user", "{bcrypt}$2a$10$hash", true);
      insertEmailAddress(userId, email, true, true);

      // When
      final Optional<User> result = emailAddressRepository.findUserByEmail(email);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id()).isEqualTo(userId);
    }

    @Test
    @DisplayName("should return inactive user")
    void shouldReturnInactiveUser() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String email = "inactive@example.com";

      insertUser(userId, "inactive.user", "{bcrypt}$2a$10$hash", false);
      insertEmailAddress(userId, email, true, true);

      // When
      final Optional<User> result = emailAddressRepository.findUserByEmail(email);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().isActive()).isFalse();
    }

    @Test
    @DisplayName("should return user even if email is not verified")
    void shouldReturnUserEvenIfEmailNotVerified() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String email = "unverified@example.com";

      insertUser(userId, "unverified.user", "{bcrypt}$2a$10$hash", true);
      insertEmailAddress(userId, email, true, false);

      // When
      final Optional<User> result = emailAddressRepository.findUserByEmail(email);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id()).isEqualTo(userId);
    }
  }

  @Nested
  @DisplayName("findUserByEmail() - Not Found Cases")
  class FindUserByEmailNotFoundCases {
    @Test
    @DisplayName("should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist() {
      // When
      final Optional<User> result =
          emailAddressRepository.findUserByEmail("nonexistent@example.com");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty when user exists but email does not")
    void shouldReturnEmptyWhenUserExistsButEmailDoesNot() {
      // Given
      final String userId = UUID.randomUUID().toString();
      insertUser(userId, "user.without.email", "{bcrypt}$2a$10$hash", true);

      // When
      final Optional<User> result =
          emailAddressRepository.findUserByEmail("nonexistent@example.com");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should be case-sensitive for email matching")
    void shouldBeCaseSensitiveForEmailMatching() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String email = "user@example.com";

      insertUser(userId, "case.user", "{bcrypt}$2a$10$hash", true);
      insertEmailAddress(userId, email, true, true);

      // When
      final Optional<User> result = emailAddressRepository.findUserByEmail("USER@EXAMPLE.COM");

      // Then
      assertThat(result).isEmpty().as("Email matching should be case-sensitive");
    }
  }

  @Nested
  @DisplayName("findUserByEmail() - Query Limit Verification")
  class FindUserByEmailLimitVerification {
    @Test
    @DisplayName("should return only one user even when multiple users have the same email")
    void shouldReturnOnlyOneUser() {
      // Given
      final String user1Id = UUID.randomUUID().toString();
      final String user2Id = UUID.randomUUID().toString();
      final String user3Id = UUID.randomUUID().toString();
      final String sharedEmail = "shared@example.com";

      insertUser(user1Id, "user1", "{bcrypt}$2a$10$hash1", true);
      insertEmailAddress(user1Id, sharedEmail, false, true);

      insertUser(user2Id, "user2", "{bcrypt}$2a$10$hash2", true);
      insertEmailAddress(user2Id, sharedEmail, false, true);

      insertUser(user3Id, "user3", "{bcrypt}$2a$10$hash3", true);
      insertEmailAddress(user3Id, sharedEmail, false, true);

      // When
      final Optional<User> result = emailAddressRepository.findUserByEmail(sharedEmail);

      // Then
      assertThat(result).isPresent().as("Should return exactly one user despite multiple matches");
    }
  }

  // Helper methods
  private void insertUser(
      final String id, final String username, final String password, final boolean isActive) {
    final String sql = "insert into users (id, username, password, is_active) values (?, ?, ?, ?)";
    jdbcTemplate.update(sql, id, username, password, isActive);
  }

  private void insertEmailAddress(
      final String userId, final String email, final boolean isPrimary, final boolean isVerified) {
    final String sql =
        "insert into user_email_address (user_id, email, is_primary, is_verified)"
            + " values (?, ?, ?, ?)";
    jdbcTemplate.update(sql, userId, email, isPrimary, isVerified);
  }
}
