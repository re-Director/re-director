package de.jensknipper.re_director.test_redirects.config;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRedirectConfiguration {

  @Bean
  public HttpClient noFollowRedirectHttpClient() {
    return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
  }
}
