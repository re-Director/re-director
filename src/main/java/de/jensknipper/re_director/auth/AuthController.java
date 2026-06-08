package de.jensknipper.re_director.auth;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
  private final UserRepository userRepository;

  public AuthController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/login")
  public String login(@Nullable Authentication authentication) {
    if (userRepository.count() == 0) {
      return "redirect:/setup";
    }
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/";
    }
    return "login";
  }
}
