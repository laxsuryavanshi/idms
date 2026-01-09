package com.turtleby.idms.web.tenant.dao;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.tenant.entity.Tenant;

public interface TenantRepository
    extends ListCrudRepository<Tenant, Long>, PagingAndSortingRepository<Tenant, Long> {
  Optional<Tenant> findBySlug(String slug);

  boolean existsBySlug(String slug);
}
