package com.turtleby.idms.web.user.dao;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.user.entity.EmailAddress;

public interface EmailAddressRepository
    extends ListCrudRepository<EmailAddress, String>,
        PagingAndSortingRepository<EmailAddress, String> {}
