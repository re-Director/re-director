package de.jensknipper.redirector.auth;

import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@ConditionalOnBooleanProperty("re-director.auth.enabled")
public class AuthViewController {
  private static final String REDIRECT_LOGIN = "redirect:/login";

  private final UserService userService;

  public AuthViewController(UserService userService) {
    this.userService = userService;
  }

  // show errors on failed login
  @GetMapping("/login")
  public String login(
      @Nullable Authentication authentication,
      Model model,
      @Nullable @RequestParam(required = false) String error) {
    if (!userService.hasUsers()) {
      return "redirect:/setup";
    }
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/";
    }
    model.addAttribute("error", error != null);
    return "login";
  }

  @GetMapping("/setup")
  public String setupForm(Model model) {
    if (userService.hasUsers()) {
      return REDIRECT_LOGIN;
    }
    model.addAttribute("form", SetupFormDto.empty());
    return "setup";
  }

  @PostMapping("/setup")
  public String handleSetup(@Valid @ModelAttribute("form") SetupFormDto form, BindingResult br) {
    if (userService.hasUsers()) {
      return REDIRECT_LOGIN;
    }

    if (!form.password().equals(form.confirmPassword())) {
      br.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
    }
    if (br.hasErrors()) {
      return "setup";
    }
    userService.createUser(form.username(), form.password());
    return REDIRECT_LOGIN;
  }
}
