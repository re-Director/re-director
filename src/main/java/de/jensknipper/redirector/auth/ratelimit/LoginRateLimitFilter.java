package de.jensknipper.redirector.auth.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

  private static final RequestMatcher LOGIN_POST_MATCHER =
      PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/login");

  private final LoginRateLimitCaches caches;

  LoginRateLimitFilter(LoginRateLimitCaches caches) {
    this.caches = caches;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    if (!LOGIN_POST_MATCHER.matches(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    String ip = request.getRemoteAddr();
    String username = request.getParameter("username");
    if (username == null) {
      username = "";
    }

    boolean allowed =
        caches.ip().tryConsume(ip)
            && caches.ipAndUsername().tryConsume(ip + ":" + username)
            && caches.username().tryConsume(username);

    if (!allowed) {
      response.setStatus(429);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
