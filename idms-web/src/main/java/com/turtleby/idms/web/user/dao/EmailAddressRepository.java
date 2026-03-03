package com.turtleby.idms.web.user.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.user.entity.EmailAddress;
import com.turtleby.idms.web.user.entity.User;

public interface EmailAddressRepository
    extends ListCrudRepository<EmailAddress, Long>, PagingAndSortingRepository<EmailAddress, Long> {
  /**
   * Finds all email addresses associated with a specific user.
   *
   * @param userId the user ID
   * @return list of email addresses for the user
   */
  List<EmailAddress> findByUserId(String userId);

  /**
   * Finds email addresses by the email value.
   *
   * @param email the email address to search for
   * @return list of matching email addresses
   */
  List<EmailAddress> findByEmail(String email);

  boolean existsByEmail(String email);

  @Query(
      "select u.id, u.username, u.password, u.is_active from users u "
          + "join user_email_address uea on u.id = uea.user_id "
          + "where uea.email = :email "
          + "order by uea.is_primary desc, u.username asc "
          + "limit 1")
  Optional<User> findUserByEmail(String email);
}
