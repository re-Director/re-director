package de.jensknipper.re_director.filter_redirects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DomainRedirectFilter extends OncePerRequestFilter {

  private final FilterRedirectsService filterRedirectsService;

  public DomainRedirectFilter(FilterRedirectsService filterRedirectsService) {
    this.filterRedirectsService = filterRedirectsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String host = request.getHeader("Host");
    if (host != null) {
      String normalizedHost = normalizeHost(host);
      RedirectInformation redirectInformation =
          filterRedirectsService.findRedirectInformationBySource(normalizedHost);
      if (redirectInformation != null) {
        response.setStatus(redirectInformation.httpStatusCode().getCode());
        response.setHeader("Location", getTarget(redirectInformation, request));
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  private String getTarget(RedirectInformation redirectInformation, HttpServletRequest request) {
    StringBuilder newUrl = new StringBuilder(redirectInformation.target());
    String path = request.getRequestURI();
    if (redirectInformation.pathForwarding() && path != null) {
      newUrl.append(path);
    }
    String query = request.getQueryString();
    if (redirectInformation.queryForwarding() && query != null) {
      newUrl.append("?");
      newUrl.append(query);
    }
    return newUrl.toString();
  }

  private String normalizeHost(String host) {
    int portStartIndex = host.indexOf(":");
    if (portStartIndex >= 0) {
      return host.substring(0, portStartIndex);
    }
    return host;
  }
}
