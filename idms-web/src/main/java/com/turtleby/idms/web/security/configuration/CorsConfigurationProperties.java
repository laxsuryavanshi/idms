package com.turtleby.idms.web.security.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for CORS settings (prefix: idms.web.cors).
 *
 * <p>Supports defining different CORS rules for different path patterns and origins. Each rule can
 * apply to specific URL paths and have different permissions for different origins.
 *
 * @param rules List of CORS rules, each applying to specific path patterns and origins. Empty by
 *     default (no CORS allowed).
 */
@ConfigurationProperties(prefix = "idms.web.cors")
public record CorsConfigurationProperties(List<CorsRule> rules) {
  public CorsConfigurationProperties {
    rules = rules != null ? List.copyOf(rules) : List.of();
  }

  /**
   * Represents a CORS rule for one or more origins and path patterns.
   *
   * @param pathPattern URL path pattern this rule applies to (e.g., "/api/**", "/**"). Defaults to
   *     "/**".
   * @param origins List of origins this rule applies to. Required.
   * @param allowedMethods List of allowed HTTP methods. Empty by default (no methods allowed).
   * @param allowedHeaders List of allowed headers. Empty by default (no headers allowed).
   * @param exposedHeaders List of headers exposed to the client. Empty by default.
   * @param allowCredentials Whether credentials (cookies, authorization headers) are supported.
   *     Defaults to false for security.
   * @param maxAge Maximum age (in seconds) of the cache duration for preflight requests. Defaults
   *     to 0 (no caching).
   */
  public record CorsRule(
      String pathPattern,
      List<String> origins,
      List<String> allowedMethods,
      List<String> allowedHeaders,
      List<String> exposedHeaders,
      boolean allowCredentials,
      long maxAge) {
    public CorsRule {
      pathPattern = pathPattern != null && !pathPattern.isBlank() ? pathPattern : "/**";
      origins = origins != null ? List.copyOf(origins) : List.of();
      allowedMethods = allowedMethods != null ? List.copyOf(allowedMethods) : List.of();
      allowedHeaders = allowedHeaders != null ? List.copyOf(allowedHeaders) : List.of();
      exposedHeaders = exposedHeaders != null ? List.copyOf(exposedHeaders) : List.of();
    }
  }
}
