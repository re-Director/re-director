package de.jensknipper.redirector.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class JteErrorViewResolver implements ErrorViewResolver {

  private static final Set<String> AVAILABLE_STATUS_VIEWS = Set.of("403", "404");

  @Override
  public @Nullable ModelAndView resolveErrorView(
      HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
    String statusCode = String.valueOf(status.value());
    if (AVAILABLE_STATUS_VIEWS.contains(statusCode)) {
      return new ModelAndView("error/" + statusCode, model);
    }
    return null;
  }
}
