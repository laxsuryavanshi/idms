package com.turtleby.idms.web.core.dao;

import com.turtleby.idms.web.core.entity.User;

/**
 * Data access interface for managing user-related database operations.
 *
 * <p>This interface defines the contract for retrieving user information from the underlying data
 * store. Implementations of this interface are responsible for handling the specifics of data
 * access, such as executing database queries or interacting with other persistence mechanisms.
 *
 * <p>This interface is primarily used by the security layer to load user details during the
 * authentication process.
 *
 * @see User
 * @see com.turtleby.idms.web.security.userdetails.UserDetailsManager
 */
public interface UserDataManager {

  /**
   * Retrieves a user by their username.
   *
   * <p>This method is used during authentication to load user credentials and account status. If
   * multiple users exist with the same username (which should be prevented by database
   * constraints), the implementation should return only one result or throw an exception.
   *
   * @param username the username to search for; must not be {@code null}
   * @return the {@link User} associated with the given username, or {@code null} if no user is
   *     found
   */
  User getUserByUsername(String username);
}
