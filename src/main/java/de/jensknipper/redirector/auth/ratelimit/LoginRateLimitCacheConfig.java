package de.jensknipper.redirector.auth.ratelimit;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import java.time.Duration;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoginRateLimitCacheConfig {

  @Bean
  LoginRateLimitCaches loginRateLimitCaches() {
    return new LoginRateLimitCaches(
        buildCache(
            "login-rate-limit-ip-username",
            "expireAfterAccess=10m,maximumSize=10000",
            perMinute(5)),
        buildCache("login-rate-limit-ip", "expireAfterAccess=10m,maximumSize=5000", perMinute(20)),
        buildCache(
            "login-rate-limit-username", "expireAfterAccess=10m,maximumSize=10000", perMinute(20)));
  }

  private RateLimitCache buildCache(String name, String caffeineSpec, Bandwidth limit) {
    return new RateLimitCache(new CaffeineCache(name, Caffeine.from(caffeineSpec).build()), limit);
  }

  private static Bandwidth perMinute(int attempts) {
    return Bandwidth.builder()
        .capacity(attempts)
        .refillGreedy(attempts, Duration.ofMinutes(1))
        .build();
  }
}
