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
@DisplayName("PhoneNumberRepository.findUserByPhoneNumber() Integration Tests")
class PhoneNumberRepositoryITests {
  @Autowired private PhoneNumberRepository phoneNumberRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @AfterEach
  void tearDown() {
    jdbcTemplate.execute("delete from user_phone_number");
    jdbcTemplate.execute("delete from users");
  }

  @Nested
  @DisplayName("findUserByPhoneNumber() - Success Cases")
  class FindUserByPhoneNumberSuccessCases {
    @Test
    @DisplayName("should return user when phone number exists")
    void shouldReturnUserWhenPhoneNumberExists() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String username = "john.doe";
      final String password = "{bcrypt}$2a$10$hashedpassword";
      final String phoneNumber = "+1234567890";

      insertUser(userId, username, password, true);
      insertPhoneNumber(userId, phoneNumber, true, true);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber(phoneNumber);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id()).isEqualTo(userId);
      assertThat(result.get().username()).isEqualTo(username);
      assertThat(result.get().password()).isEqualTo(password);
      assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("should return user with primary phone when multiple users have same phone number")
    void shouldReturnUserWithPrimaryPhone() {
      // Given
      final String user1Id = UUID.randomUUID().toString();
      final String user2Id = UUID.randomUUID().toString();
      final String sharedPhone = "+1234567890";

      // User 1 has the phone as non-primary
      insertUser(user1Id, "user1", "{bcrypt}$2a$10$hash1", true);
      insertPhoneNumber(user1Id, sharedPhone, false, true);

      // User 2 has the phone as primary
      insertUser(user2Id, "user2", "{bcrypt}$2a$10$hash2", true);
      insertPhoneNumber(user2Id, sharedPhone, true, true);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber(sharedPhone);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id())
          .isEqualTo(user2Id)
          .as("Should return the user with primary phone number");
      assertThat(result.get().username()).isEqualTo("user2");
    }

    @Test
    @DisplayName(
        "should return user with alphabetically first username when multiple phones with same"
            + " priority")
    void shouldReturnUserWithAlphabeticallyFirstUsername() {
      // Given
      final String user1Id = UUID.randomUUID().toString();
      final String user2Id = UUID.randomUUID().toString();
      final String sharedPhone = "+1234567890";

      // Both users have the phone as non-primary (same priority)
      insertUser(user1Id, "zebra", "{bcrypt}$2a$10$hash1", true);
      insertPhoneNumber(user1Id, sharedPhone, false, true);

      insertUser(user2Id, "alpha", "{bcrypt}$2a$10$hash2", true);
      insertPhoneNumber(user2Id, sharedPhone, false, true);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber(sharedPhone);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().username())
          .isEqualTo("alpha")
          .as("Should return user with alphabetically first username");
    }

    @Test
    @DisplayName("should handle phone number with various formats")
    void shouldHandlePhoneNumberWithVariousFormats() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String phoneNumber = "+1-555-123-4567";

      insertUser(userId, "formatted.user", "{bcrypt}$2a$10$hash", true);
      insertPhoneNumber(userId, phoneNumber, true, true);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber(phoneNumber);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id()).isEqualTo(userId);
    }

    @Test
    @DisplayName("should return inactive user")
    void shouldReturnInactiveUser() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String phoneNumber = "+9876543210";

      insertUser(userId, "inactive.user", "{bcrypt}$2a$10$hash", false);
      insertPhoneNumber(userId, phoneNumber, true, true);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber(phoneNumber);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().isActive()).isFalse();
    }

    @Test
    @DisplayName("should return user even if phone number is not verified")
    void shouldReturnUserEvenIfPhoneNotVerified() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String phoneNumber = "+1111111111";

      insertUser(userId, "unverified.user", "{bcrypt}$2a$10$hash", true);
      insertPhoneNumber(userId, phoneNumber, true, false);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber(phoneNumber);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id()).isEqualTo(userId);
    }

    @Test
    @DisplayName("should handle international phone numbers")
    void shouldHandleInternationalPhoneNumbers() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String phoneNumber = "+44-7700-900123";

      insertUser(userId, "uk.user", "{bcrypt}$2a$10$hash", true);
      insertPhoneNumber(userId, phoneNumber, true, true);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber(phoneNumber);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().id()).isEqualTo(userId);
    }
  }

  @Nested
  @DisplayName("findUserByPhoneNumber() - Not Found Cases")
  class FindUserByPhoneNumberNotFoundCases {
    @Test
    @DisplayName("should return empty when phone number does not exist")
    void shouldReturnEmptyWhenPhoneNumberDoesNotExist() {
      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber("+9999999999");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty when user exists but phone number does not")
    void shouldReturnEmptyWhenUserExistsButPhoneDoesNot() {
      // Given
      final String userId = UUID.randomUUID().toString();
      insertUser(userId, "user.without.phone", "{bcrypt}$2a$10$hash", true);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber("+9999999999");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should be case-sensitive and format-sensitive for phone number matching")
    void shouldBeExactMatchForPhoneNumber() {
      // Given
      final String userId = UUID.randomUUID().toString();
      final String phoneNumber = "+1234567890";

      insertUser(userId, "exact.user", "{bcrypt}$2a$10$hash", true);
      insertPhoneNumber(userId, phoneNumber, true, true);

      // When - trying with different format
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber("1234567890");

      // Then
      assertThat(result).isEmpty().as("Phone number matching should be exact");
    }
  }

  @Nested
  @DisplayName("findUserByPhoneNumber() - Query Limit Verification")
  class FindUserByPhoneNumberLimitVerification {
    @Test
    @DisplayName("should return only one user even when multiple users have the same phone number")
    void shouldReturnOnlyOneUser() {
      // Given
      final String user1Id = UUID.randomUUID().toString();
      final String user2Id = UUID.randomUUID().toString();
      final String user3Id = UUID.randomUUID().toString();
      final String sharedPhone = "+1234567890";

      insertUser(user1Id, "user1", "{bcrypt}$2a$10$hash1", true);
      insertPhoneNumber(user1Id, sharedPhone, false, true);

      insertUser(user2Id, "user2", "{bcrypt}$2a$10$hash2", true);
      insertPhoneNumber(user2Id, sharedPhone, false, true);

      insertUser(user3Id, "user3", "{bcrypt}$2a$10$hash3", true);
      insertPhoneNumber(user3Id, sharedPhone, false, true);

      // When
      final Optional<User> result = phoneNumberRepository.findUserByPhoneNumber(sharedPhone);

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

  private void insertPhoneNumber(
      final String userId,
      final String phoneNumber,
      final boolean isPrimary,
      final boolean isVerified) {
    final String sql =
        "insert into user_phone_number (user_id, phone_number, is_primary, is_verified)"
            + " values (?, ?, ?, ?)";
    jdbcTemplate.update(sql, userId, phoneNumber, isPrimary, isVerified);
  }
}
