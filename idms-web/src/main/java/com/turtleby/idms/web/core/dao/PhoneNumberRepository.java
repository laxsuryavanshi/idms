package com.turtleby.idms.web.core.dao;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.core.entity.PhoneNumber;

public interface PhoneNumberRepository
    extends ListCrudRepository<PhoneNumber, String>,
        PagingAndSortingRepository<PhoneNumber, String> {}
