package com.turtleby.idms.web.user.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * User Aggregate Root Entity following Domain-Driven Design principles.
 *
 * <p>This aggregate encapsulates the {@link User} entity along with its associated entities
 * including {@link UserProfile}, {@link EmailAddress}, and {@link PhoneNumber}. It provides a
 * unified interface to access and manage all user-related data while maintaining consistency and
 * enforcing business rules.
 *
 * <p>The aggregate ensures that:
 *
 * <ul>
 *   <li>All changes to user-related entities go through the aggregate root
 *   <li>Business invariants are maintained across the aggregate boundary
 *   <li>Related entities are loaded together to avoid N+1 query problems
 *   <li>Transactional consistency is maintained within the aggregate
 * </ul>
 *
 * <p>This class is immutable by default; modifications should be done through builder methods that
 * return new instances.
 *
 * @see User
 * @see UserProfile
 * @see EmailAddress
 * @see PhoneNumber
 * @see UserAccount
 */
public class UserAggregate implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private final User user;
  private final UserProfile profile;
  private final List<EmailAddress> emailAddresses;
  private final List<PhoneNumber> phoneNumbers;
  private final List<UserAccount> userAccounts;

  private UserAggregate(
      final User user,
      final UserProfile profile,
      final List<EmailAddress> emailAddresses,
      final List<PhoneNumber> phoneNumbers,
      final List<UserAccount> userAccounts) {
    this.user = Objects.requireNonNull(user, "user must not be null");
    this.profile = profile;
    this.emailAddresses =
        emailAddresses != null
            ? Collections.unmodifiableList(new ArrayList<>(emailAddresses))
            : Collections.emptyList();
    this.phoneNumbers =
        phoneNumbers != null
            ? Collections.unmodifiableList(new ArrayList<>(phoneNumbers))
            : Collections.emptyList();
    this.userAccounts =
        userAccounts != null
            ? Collections.unmodifiableList(new ArrayList<>(userAccounts))
            : Collections.emptyList();
  }

  /**
   * Gets the core user entity containing authentication credentials.
   *
   * @return the user entity (never null)
   */
  public User getUser() {
    return user;
  }

  /**
   * Gets the user's unique identifier.
   *
   * @return the user ID
   */
  public String getUserId() {
    return user.id();
  }

  /**
   * Gets the username used for authentication.
   *
   * @return the username
   */
  public String getUsername() {
    return user.username();
  }

  /**
   * Checks if the user account is active.
   *
   * @return true if the user is active, false otherwise
   */
  public boolean isActive() {
    return user.isActive();
  }

  /**
   * Gets the user profile containing additional user information.
   *
   * @return an Optional containing the user profile if it exists, or empty if no profile is set
   */
  public Optional<UserProfile> getProfile() {
    return Optional.ofNullable(profile);
  }

  /**
   * Gets all email addresses associated with the user.
   *
   * @return an unmodifiable list of email addresses (never null)
   */
  public List<EmailAddress> getEmailAddresses() {
    return emailAddresses;
  }

  /**
   * Gets the primary email address for the user.
   *
   * @return an Optional containing the primary email address if it exists, or empty if no primary
   *     email is set
   */
  public Optional<EmailAddress> getPrimaryEmail() {
    return emailAddresses.stream().filter(EmailAddress::isPrimary).findFirst();
  }

  /**
   * Gets all verified email addresses for the user.
   *
   * @return a list of verified email addresses (never null)
   */
  public List<EmailAddress> getVerifiedEmails() {
    return emailAddresses.stream().filter(EmailAddress::isVerified).toList();
  }

  /**
   * Gets all phone numbers associated with the user.
   *
   * @return an unmodifiable list of phone numbers (never null)
   */
  public List<PhoneNumber> getPhoneNumbers() {
    return phoneNumbers;
  }

  /**
   * Gets the primary phone number for the user.
   *
   * @return an Optional containing the primary phone number if it exists, or empty if no primary
   *     phone is set
   */
  public Optional<PhoneNumber> getPrimaryPhone() {
    return phoneNumbers.stream().filter(PhoneNumber::isPrimary).findFirst();
  }

  /**
   * Gets all verified phone numbers for the user.
   *
   * @return a list of verified phone numbers (never null)
   */
  public List<PhoneNumber> getVerifiedPhones() {
    return phoneNumbers.stream().filter(PhoneNumber::isVerified).toList();
  }

  /**
   * Gets all linked authentication accounts for the user.
   *
   * @return an unmodifiable list of user accounts (never null)
   */
  public List<UserAccount> getUserAccounts() {
    return userAccounts;
  }

  /**
   * Gets the user account associated with the given provider.
   *
   * @param provider the provider identifier (e.g. "google", "github")
   * @return an Optional containing the matching UserAccount, or empty if not found
   */
  public Optional<UserAccount> getAccountByProvider(final String provider) {
    return userAccounts.stream()
        .filter(account -> account.provider().equalsIgnoreCase(provider))
        .findFirst();
  }

  /**
   * Checks if the user has at least one linked external account.
   *
   * @return true if the user has at least one user account, false otherwise
   */
  public boolean hasLinkedAccount() {
    return !userAccounts.isEmpty();
  }

  /**
   * Checks if the user has a complete profile.
   *
   * @return true if the user has a profile set, false otherwise
   */
  public boolean hasProfile() {
    return profile != null;
  }

  /**
   * Checks if the user has at least one verified email address.
   *
   * @return true if at least one email is verified, false otherwise
   */
  public boolean hasVerifiedEmail() {
    return emailAddresses.stream().anyMatch(EmailAddress::isVerified);
  }

  /**
   * Checks if the user has at least one verified phone number.
   *
   * @return true if at least one phone number is verified, false otherwise
   */
  public boolean hasVerifiedPhone() {
    return phoneNumbers.stream().anyMatch(PhoneNumber::isVerified);
  }

  /**
   * Gets the display name for the user. Returns the profile name if available, otherwise falls back
   * to the username.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return getProfile().map(UserProfile::name).orElse(user.username());
  }

  /**
   * Gets the preferred username. Returns the profile's preferred username if available, otherwise
   * falls back to the authentication username.
   *
   * @return the preferred username
   */
  public String getPreferredUsername() {
    return getProfile().map(UserProfile::preferredUsername).orElse(user.username());
  }

  /**
   * Creates a new builder for constructing UserAggregate instances.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new builder initialized with the values from this aggregate. Useful for creating
   * modified copies.
   *
   * @return a new builder instance initialized with current values
   */
  public Builder toBuilder() {
    return new Builder()
        .user(this.user)
        .profile(this.profile)
        .emailAddresses(emails -> emails.addAll(this.emailAddresses))
        .phoneNumbers(phones -> phones.addAll(this.phoneNumbers))
        .userAccounts(accounts -> accounts.addAll(this.userAccounts));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof final UserAggregate that)) {
      return false;
    }
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }

  /** Builder for constructing UserAggregate instances. */
  public static class Builder {
    private User user;
    private UserProfile profile;
    private final List<EmailAddress> emailAddresses = new ArrayList<>();
    private final List<PhoneNumber> phoneNumbers = new ArrayList<>();
    private final List<UserAccount> userAccounts = new ArrayList<>();

    private Builder() {}

    /**
     * Sets the user entity.
     *
     * @param user the user entity (required)
     * @return this builder instance
     * @throws NullPointerException if user is null
     */
    public Builder user(final User user) {
      this.user = Objects.requireNonNull(user, "user must not be null");
      return this;
    }

    /**
     * Sets the user profile.
     *
     * @param profile the user profile (optional)
     * @return this builder instance
     */
    public Builder profile(final UserProfile profile) {
      this.profile = profile;
      return this;
    }

    /**
     * Configures the email addresses for the user using a consumer function. This method allows
     * batch operations on the email addresses list, such as adding multiple emails at once or
     * clearing the list.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * builder.emailAddresses(emails -> {
     *   emails.add(primaryEmail);
     *   emails.add(secondaryEmail);
     * });
     * }</pre>
     *
     * @param consumer the consumer function that accepts the mutable email addresses list
     * @return this builder instance
     * @throws NullPointerException if consumer is null
     */
    public Builder emailAddresses(final Consumer<List<EmailAddress>> consumer) {
      Objects.requireNonNull(consumer, "consumer must not be null");
      consumer.accept(this.emailAddresses);
      return this;
    }

    /**
     * Adds a single email address to the aggregate.
     *
     * @param emailAddress the email address to add
     * @return this builder instance
     * @throws NullPointerException if emailAddress is null
     */
    public Builder emailAddress(final EmailAddress emailAddress) {
      Objects.requireNonNull(emailAddress, "emailAddress must not be null");
      this.emailAddresses.add(emailAddress);
      return this;
    }

    /**
     * Configures the phone numbers for the user using a consumer function. This method allows batch
     * operations on the phone numbers list, such as adding multiple phone numbers at once or
     * clearing the list.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * builder.phoneNumbers(phones -> {
     *   phones.add(mobilePhone);
     *   phones.add(workPhone);
     * });
     * }</pre>
     *
     * @param consumer the consumer function that accepts the mutable phone numbers list
     * @return this builder instance
     * @throws NullPointerException if consumer is null
     */
    public Builder phoneNumbers(final Consumer<List<PhoneNumber>> consumer) {
      Objects.requireNonNull(consumer, "consumer must not be null");
      consumer.accept(this.phoneNumbers);
      return this;
    }

    /**
     * Adds a single phone number to the aggregate.
     *
     * @param phoneNumber the phone number to add
     * @return this builder instance
     * @throws NullPointerException if phoneNumber is null
     */
    public Builder phoneNumber(final PhoneNumber phoneNumber) {
      Objects.requireNonNull(phoneNumber, "phoneNumber must not be null");
      this.phoneNumbers.add(phoneNumber);
      return this;
    }

    /**
     * Configures the user accounts for the user using a consumer function. This method allows batch
     * operations on the user accounts list, such as adding multiple accounts at once or clearing
     * the list.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * builder.userAccounts(accounts -> {
     *   accounts.add(googleAccount);
     *   accounts.add(githubAccount);
     * });
     * }</pre>
     *
     * @param consumer the consumer function that accepts the mutable user accounts list
     * @return this builder instance
     * @throws NullPointerException if consumer is null
     */
    public Builder userAccounts(final Consumer<List<UserAccount>> consumer) {
      Objects.requireNonNull(consumer, "consumer must not be null");
      consumer.accept(this.userAccounts);
      return this;
    }

    /**
     * Adds a single user account to the aggregate.
     *
     * @param userAccount the user account to add
     * @return this builder instance
     * @throws NullPointerException if userAccount is null
     */
    public Builder userAccount(final UserAccount userAccount) {
      Objects.requireNonNull(userAccount, "userAccount must not be null");
      this.userAccounts.add(userAccount);
      return this;
    }

    /**
     * Builds the UserAggregate instance.
     *
     * @return the constructed UserAggregate
     * @throws NullPointerException if user is null
     */
    public UserAggregate build() {
      return new UserAggregate(user, profile, emailAddresses, phoneNumbers, userAccounts);
    }
  }
}
