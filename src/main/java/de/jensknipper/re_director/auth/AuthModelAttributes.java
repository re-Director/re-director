package de.jensknipper.re_director.auth;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AuthModelAttributes {

  @ModelAttribute
  public void addCsrfToken(Model model, CsrfToken csrfToken) {
    model.addAttribute("csrfToken", csrfToken);
  }
}
