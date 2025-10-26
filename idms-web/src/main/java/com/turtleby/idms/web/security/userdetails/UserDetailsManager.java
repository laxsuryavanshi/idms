package com.turtleby.idms.web.security.userdetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.turtleby.idms.web.core.dao.UserDataManager;
import com.turtleby.idms.web.core.entity.User;

public class UserDetailsManager implements UserDetailsService {
  private final UserDataManager userDataManager;

  public UserDetailsManager(final UserDataManager userDataManager) {
    this.userDataManager = userDataManager;
  }

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    final User user = userDataManager.getUserByUsername(username);

    if (user == null) {
      throw new UsernameNotFoundException("User with username '" + username + "' not found");
    }

    return new SecurityUser(user);
  }
}
