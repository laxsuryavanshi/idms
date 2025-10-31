create table "users" (
  "id" char(36) not null,
  "username" varchar(255) not null,
  "password" varchar(255) not null,
  "is_active" boolean not null,
  constraint "users_pkey" primary key ("id"),
  constraint "users_username_key" unique ("username")
);
