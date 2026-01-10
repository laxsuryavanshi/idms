package com.turtleby.idms.web.security.configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idms.security.jwk")
public record KeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey, String keyId) {}
