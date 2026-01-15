package com.turtleby.idms.web.user.dao;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.user.entity.User;

public interface UserRepository
    extends ListCrudRepository<User, String>, PagingAndSortingRepository<User, String> {
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);
}
