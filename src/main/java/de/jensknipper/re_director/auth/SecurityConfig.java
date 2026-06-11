package de.jensknipper.re_director.auth;

import de.jensknipper.re_director.filter_redirects.DomainRedirectFilter;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${re-director.auth.enabled:false}")
  private boolean authEnabled;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthProperties authProperties, DomainRedirectFilter redirectFilter) {
    if (!authEnabled) {
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }

    String[] allowedEndpoints =
        Stream.concat(
                Stream.of(
                    "/",
                    "/login",
                    "/logout",
                    "/setup",
                    "/no-redirect-found",
                    "/webjars/**",
                    "/img/**",
                    "/css/**",
                    "/js/**"),
                authProperties.additionalPermitAllPaths().stream())
            .toArray(String[]::new);

    http.addFilterBefore(redirectFilter, UsernamePasswordAuthenticationFilter.class);

    http.authorizeHttpRequests(
            auth -> auth.requestMatchers(allowedEndpoints).permitAll().anyRequest().authenticated())
        .formLogin(form -> form.loginPage("/login").permitAll())
        .logout(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
    return config.getAuthenticationManager();
  }
}
