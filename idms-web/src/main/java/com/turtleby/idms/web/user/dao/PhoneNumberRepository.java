package com.turtleby.idms.web.user.dao;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.user.entity.PhoneNumber;

public interface PhoneNumberRepository
    extends ListCrudRepository<PhoneNumber, String>,
        PagingAndSortingRepository<PhoneNumber, String> {}
