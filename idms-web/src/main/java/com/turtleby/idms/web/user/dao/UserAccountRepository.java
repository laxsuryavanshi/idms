package com.turtleby.idms.web.user.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.user.entity.UserAccount;

public interface UserAccountRepository
    extends ListCrudRepository<UserAccount, Long>, PagingAndSortingRepository<UserAccount, Long> {
  List<UserAccount> findByUserId(String userId);

  Optional<UserAccount> findByProviderAndProviderAccountId(
      String provider, String providerAccountId);
}
