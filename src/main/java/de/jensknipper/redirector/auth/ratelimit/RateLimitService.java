package de.jensknipper.redirector.auth.ratelimit;

import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
  private final LoginRateLimitCaches loginRateLimitCaches;

  public RateLimitService(LoginRateLimitCaches loginRateLimitCaches) {
    this.loginRateLimitCaches = loginRateLimitCaches;
  }

  public void clearLoginRateLimitCaches() {
    loginRateLimitCaches.clearAll();
  }
}
