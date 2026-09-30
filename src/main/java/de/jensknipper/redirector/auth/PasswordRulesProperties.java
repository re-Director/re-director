package de.jensknipper.redirector.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "re-director.auth.password")
public record PasswordRulesProperties(int minLength) {}
