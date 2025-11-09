package com.turtleby.idms.web.core.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_profile")
public record UserProfile(
    @Id String id,
    String sub,
    String name,
    String givenName,
    String familyName,
    String middleName,
    String nickname,
    String preferredUsername,
    String profile,
    String picture,
    String website,
    String email,
    Boolean emailVerified,
    String gender,
    Date birthdate,
    String zoneinfo,
    String locale,
    String phoneNumber,
    Boolean phoneNumberVerified,
    Address address,
    Instant updatedAt)
    implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  public static record Address(
      String formatted,
      String street,
      String locality,
      String region,
      String postalCode,
      String country)
      implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
  }
}
