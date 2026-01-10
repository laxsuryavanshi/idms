package com.turtleby.idms.web.security.configuration;

import static com.turtleby.idms.web.common.URIConstants.LOGIN_URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({KeyProperties.class})
public class AuthorizationServerSecurityConfiguration {
  @Bean
  @Order(SecurityOrder.AUTHORIZATION_SERVER)
  public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http)
      throws Exception {
    OAuth2AuthorizationServerConfigurer configurer =
        OAuth2AuthorizationServerConfigurer.authorizationServer();

    return http.securityMatcher(configurer.getEndpointsMatcher())
        .with(configurer, (server) -> server.oidc(Customizer.withDefaults()))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
        .exceptionHandling(
            exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint(LOGIN_URI),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
        .build();
  }

  @Bean
  public RegisteredClientRepository registeredClientRepository(final JdbcTemplate jdbcTemplate) {
    return new JdbcRegisteredClientRepository(jdbcTemplate);
  }

  @Bean
  public JWKSource<SecurityContext> jwkSource(final KeyProperties keyProperties) {
    final RSAKey rsaKey =
        new RSAKey.Builder(keyProperties.publicKey())
            .privateKey(keyProperties.privateKey())
            .keyID(keyProperties.keyId())
            .build();
    final JWKSet jwkSet = new JWKSet(rsaKey);
    return new ImmutableJWKSet<>(jwkSet);
  }

  @Bean
  public JwtDecoder jwtDecoder(final JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder().build();
  }
}
