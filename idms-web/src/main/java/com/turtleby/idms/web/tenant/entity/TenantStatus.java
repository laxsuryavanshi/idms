package com.turtleby.idms.web.tenant.entity;

public enum TenantStatus {
  ACTIVE("active"),
  SUSPENDED("suspended"),
  PENDING_ACTIVATION("pending_activation");

  private final String value;

  TenantStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
