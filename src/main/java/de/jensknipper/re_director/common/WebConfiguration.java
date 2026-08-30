package de.jensknipper.re_director.common;

import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfiguration {

  @Bean
  public FilterRegistrationBean<Filter> trailingSlashFilter() {
    Filter filter = UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    return registration;
  }
}
