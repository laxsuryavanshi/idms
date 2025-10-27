package com.turtleby.idms.web.security.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

@Configuration(proxyBeanMethods = false)
@EnableRedisIndexedHttpSession
public class SessionConfiguration {}
