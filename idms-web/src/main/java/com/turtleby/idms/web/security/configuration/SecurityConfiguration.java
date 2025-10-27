package com.turtleby.idms.web.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.turtleby.idms.web.core.dao.UserDataManager;
import com.turtleby.idms.web.security.userdetails.UserDetailsManager;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfiguration {
  @Bean
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .oauth2Login(Customizer.withDefaults())
        .build();
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
