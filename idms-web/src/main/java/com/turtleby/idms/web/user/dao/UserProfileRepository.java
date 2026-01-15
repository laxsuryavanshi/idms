package com.turtleby.idms.web.user.dao;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.user.entity.UserProfile;

public interface UserProfileRepository
    extends ListCrudRepository<UserProfile, String>,
        PagingAndSortingRepository<UserProfile, String> {
  Optional<UserProfile> findBySub(String sub);
}
