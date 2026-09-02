package de.jensknipper.redirector.redirects.test.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRedirectConfiguration {

  @Bean
  public OkHttpClient noFollowRedirectHttpClient() {
    return new OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).build();
  }
}
