package com.turtleby.idms.web.user.entity;

public enum AuthenticationProviderType {
  OAUTH2("oauth2"),
  OIDC("oidc"),
  SAML2("saml2"),
  MAGIC_LINK("magic_link"),
  WEBAUTHN("webauthn"),
  CREDENTIALS("credentials");

  private final String type;

  AuthenticationProviderType(final String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
