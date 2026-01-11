package com.turtleby.idms.web.user.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.turtleby.idms.web.user.dao.JdbcUserDataManager;
import com.turtleby.idms.web.user.dao.UserDataManager;

@Configuration(proxyBeanMethods = false)
public class UserDataManagerConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public UserDataManager userDataManager(final JdbcTemplate jdbcTemplate) {
    return new JdbcUserDataManager(jdbcTemplate);
  }
}
