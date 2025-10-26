package com.turtleby.idms.web.core.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.turtleby.idms.web.core.dao.JdbcUserDataManager;
import com.turtleby.idms.web.core.dao.UserDataManager;

@Configuration(proxyBeanMethods = false)
public class CoreDataManagerConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public UserDataManager userDataManager(final JdbcTemplate jdbcTemplate) {
    return new JdbcUserDataManager(jdbcTemplate);
  }
}
