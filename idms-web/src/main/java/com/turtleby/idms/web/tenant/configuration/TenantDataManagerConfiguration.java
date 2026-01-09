package com.turtleby.idms.web.tenant.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.turtleby.idms.web.tenant.dao.TenantRepository;
import com.turtleby.idms.web.tenant.service.TenantManager;
import com.turtleby.idms.web.tenant.service.TenantService;

@Configuration(proxyBeanMethods = false)
public class TenantDataManagerConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public TenantManager tenantManager(final TenantRepository tenantRepository) {
    return new TenantService(tenantRepository);
  }
}
