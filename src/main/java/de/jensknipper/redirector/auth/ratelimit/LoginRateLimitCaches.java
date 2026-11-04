package de.jensknipper.redirector.auth.ratelimit;

public record LoginRateLimitCaches(
    RateLimitCache ipAndUsername, RateLimitCache ip, RateLimitCache username) {

  public void clearAll() {
    ipAndUsername.clear();
    ip.clear();
    username.clear();
  }
}
