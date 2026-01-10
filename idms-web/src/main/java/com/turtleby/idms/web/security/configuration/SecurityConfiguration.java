package com.turtleby.idms.web.security.configuration;

import static com.turtleby.idms.web.common.URIConstants.ERROR_URI;
import static com.turtleby.idms.web.common.URIConstants.LOGIN_URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.turtleby.idms.web.core.dao.UserDataManager;
import com.turtleby.idms.web.security.userdetails.UserDetailsManager;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties({CorsConfigurationProperties.class})
public class SecurityConfiguration {
  @Bean
  @Order(SecurityOrder.DEFAULT)
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(LOGIN_URI, ERROR_URI)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(Customizer.withDefaults())
        .oauth2Login(Customizer.withDefaults())
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      final CorsConfigurationProperties corsProperties) {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    for (CorsConfigurationProperties.CorsRule rule : corsProperties.rules()) {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOrigins(rule.origins());
      configuration.setAllowedMethods(rule.allowedMethods());
      configuration.setAllowedHeaders(rule.allowedHeaders());
      configuration.setExposedHeaders(rule.exposedHeaders());
      configuration.setAllowCredentials(rule.allowCredentials());
      configuration.setMaxAge(rule.maxAge());

      source.registerCorsConfiguration(rule.pathPattern(), configuration);
    }

    return source;
  }

  @Bean
  public UserDetailsService userDetailsService(final UserDataManager userDataManager) {
    return new UserDetailsManager(userDataManager);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}
