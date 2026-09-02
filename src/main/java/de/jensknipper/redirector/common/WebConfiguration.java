package de.jensknipper.redirector.common;

import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfiguration {

  @Bean
  public Filter trailingSlashFilter() {
    return UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();
  }
}
