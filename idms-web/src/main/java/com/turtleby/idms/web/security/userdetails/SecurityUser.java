package com.turtleby.idms.web.security.userdetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.turtleby.idms.web.core.entity.User;

public class SecurityUser implements UserDetails {
  @Serial private static final long serialVersionUID = 1L;

  private final User user;

  public SecurityUser(final User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptySet();
  }

  @Override
  public String getPassword() {
    return user.password();
  }

  @Override
  public String getUsername() {
    return user.username();
  }

  @Override
  public boolean isEnabled() {
    return user.isActive();
  }

  public User getUser() {
    return user;
  }
}
