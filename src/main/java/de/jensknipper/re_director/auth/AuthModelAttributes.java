package de.jensknipper.re_director.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AuthModelAttributes {

  @ModelAttribute
  public void addCsrfToken(Model model, HttpServletRequest request) {
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    model.addAttribute("csrfToken", csrfToken);
  }

  @ModelAttribute
  public void addAuthEnabled(
      Model model, @Value("${re-director.auth.enabled:false}") boolean authEnabled) {
    model.addAttribute("authEnabled", authEnabled);
  }

  @ModelAttribute
  public void addIsAuthenticated(Model model, @Nullable Authentication authentication) {
    model.addAttribute("authenticated", authentication != null && authentication.isAuthenticated());
  }
}
