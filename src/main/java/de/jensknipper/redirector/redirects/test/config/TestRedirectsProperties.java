package de.jensknipper.redirector.redirects.test.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "re-director.test-redirects")
public record TestRedirectsProperties(int maxRedirects) {}
