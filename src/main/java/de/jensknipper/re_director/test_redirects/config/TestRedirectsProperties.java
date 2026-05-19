package de.jensknipper.re_director.test_redirects.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "re-director.test-redirects")
public record TestRedirectsProperties(int maxRedirects) {}
