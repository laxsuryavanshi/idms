package com.turtleby.idms.web.user.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.turtleby.idms.web.user.entity.PhoneNumber;
import com.turtleby.idms.web.user.entity.User;

public interface PhoneNumberRepository
    extends ListCrudRepository<PhoneNumber, Long>, PagingAndSortingRepository<PhoneNumber, Long> {
  /**
   * Finds all phone numbers associated with a specific user.
   *
   * @param userId the user ID
   * @return list of phone numbers for the user
   */
  List<PhoneNumber> findByUserId(String userId);

  /**
   * Finds phone numbers by the phone number value.
   *
   * @param phoneNumber the phone number to search for
   * @return list of matching phone numbers
   */
  List<PhoneNumber> findByPhoneNumber(String phoneNumber);

  boolean existsByPhoneNumber(String phoneNumber);

  @Query(
      "select u.id, u.username, u.password, u.is_active from users u "
          + "join user_phone_number upn on u.id = upn.user_id "
          + "where upn.phone_number = :phoneNumber "
          + "order by upn.is_primary desc, u.username asc "
          + "limit 1")
  Optional<User> findUserByPhoneNumber(String phoneNumber);
}
