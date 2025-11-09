-- see https://openid.net/specs/openid-connect-core-1_0.html#Claims
create table "user_profile" (
  "id" char(36) not null,
  "sub" char(36) not null,
  "name" varchar(255),
  "given_name" varchar(255),
  "family_name" varchar(255),
  "middle_name" varchar(255),
  "nickname" varchar(255),
  "preferred_username" varchar(255),
  "profile" varchar(512),
  "picture" varchar(512),
  "website" varchar(512),
  "email" varchar(255),
  "email_verified" boolean,
  "gender" varchar(50),
  "birthdate" date,
  "zoneinfo" varchar(100),
  "locale" varchar(50),
  "phone_number" varchar(20),
  "phone_number_verified" boolean,
  "address" varchar(512),
  "updated_at" timestamp,
  constraint "user_profile_pkey" primary key ("id"),
  constraint "user_profile_sub_key" unique ("sub"),
  constraint "user_profile_sub_fkey" foreign key ("sub")
    references "users" ("id") on delete cascade
);

create table "user_email_address" (
  "id" char(36) not null,
  "user_id" char(36) not null,
  "email" varchar(255) not null,
  "is_primary" boolean not null,
  "is_verified" boolean not null,
  constraint "user_email_address_pkey" primary key ("id"),
  constraint "user_email_address_user_id_fkey" foreign key ("user_id")
    references "users" ("id") on delete cascade
);

create index "user_email_address_user_id_idx" on "user_email_address" ("user_id");
create index "user_email_address_email_idx" on "user_email_address" ("email");

create table "user_phone_number" (
  "id" char(36) not null,
  "user_id" char(36) not null,
  "phone_number" varchar(20) not null,
  "is_primary" boolean not null,
  "is_verified" boolean not null,
  constraint "user_phone_number_pkey" primary key ("id"),
  constraint "user_phone_number_user_id_fkey" foreign key ("user_id")
    references "users" ("id") on delete cascade
);

create index "user_phone_number_user_id_idx" on "user_phone_number" ("user_id");
create index "user_phone_number_phone_number_idx" on "user_phone_number" ("phone_number");
