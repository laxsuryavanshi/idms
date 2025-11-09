package com.turtleby.idms.web.core.dao;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.core.entity.User;

public interface UserRepository
    extends ListCrudRepository<User, String>, PagingAndSortingRepository<User, String> {
  Optional<User> findByUsername(String username);
}
