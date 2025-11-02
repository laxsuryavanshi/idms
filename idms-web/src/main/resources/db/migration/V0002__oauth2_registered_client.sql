create table "oauth2_registered_client" (
  "id" char(36) not null,
  "client_id" varchar(255) not null,
  "client_id_issued_at" timestamp not null,
  "client_secret" varchar(255) not null,
  "client_secret_expires_at" timestamp,
  "client_name" varchar(255) not null,
  "client_authentication_methods" text not null,
  "authorization_grant_types" text not null,
  "redirect_uris" text not null,
  "post_logout_redirect_uris" text,
  "scopes" text not null,
  "client_settings" text not null,
  "token_settings" text not null,
  constraint "oauth2_registered_client_pkey" primary key ("id"),
  constraint "oauth2_registered_client_client_id_key" unique ("client_id")
);
