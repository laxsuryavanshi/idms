package com.turtleby.idms.web;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
  private static final String POSTGRES_IMAGE = "postgres:17.5-alpine3.22";
  private static final String REDIS_IMAGE = "redis:8.2.1-alpine3.22";

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE));
  }

  @Bean
  @ServiceConnection(name = "redis")
  @SuppressWarnings("resource")
  GenericContainer<?> redisContainer() {
    return new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE)).withExposedPorts(6379);
  }
}
