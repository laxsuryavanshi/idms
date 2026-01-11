package com.turtleby.idms.web.user.dao;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.user.entity.UserProfile;

public interface UserProfileRepository
    extends ListCrudRepository<UserProfile, String>,
        PagingAndSortingRepository<UserProfile, String> {}
