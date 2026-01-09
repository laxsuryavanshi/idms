package com.turtleby.idms.web.common;

public final class URIConstants {
  public static final String API_VERSION = "v1";
  public static final String API_PREFIX = "/api/" + API_VERSION;

  public static final String TENANTS_URI = "/tenants";
  public static final String TENANTS_RESOURCE_URI = API_PREFIX + TENANTS_URI;
  public static final String TENANTS_RESOURCE_URI_SUFFIX = "/{tenantId}";
  public static final String TENANTS_URI_FORMAT = TENANTS_RESOURCE_URI + "/%s";

  private URIConstants() {}
}
