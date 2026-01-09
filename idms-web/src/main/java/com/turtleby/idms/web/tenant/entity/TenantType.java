package com.turtleby.idms.web.tenant.entity;

public enum TenantType {
  SYSDEFAULT("sysdefault"),
  STANDARD("standard");

  private final String value;

  TenantType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
