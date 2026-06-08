package de.jensknipper.re_director.auth;

import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@ConditionalOnBooleanProperty("re-director.auth.enabled")
public class AuthController {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
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

  @GetMapping("/setup")
  public String setupForm(Model model) {
    if (userRepository.count() > 0) {
      return "redirect:/login";
    }
    model.addAttribute("form", new SetupFormDto());
    return "setup";
  }

  @PostMapping("/setup")
  public String handleSetup(@Valid @ModelAttribute("form") SetupFormDto form, BindingResult br) {
    if (userRepository.count() > 0) {
      return "redirect:/login";
    }

    if (!form.getPassword().equals(form.getConfirmPassword())) {
      br.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
    }
    if (br.hasErrors()) {
      return "setup";
    }
    String hash = passwordEncoder.encode(form.getPassword());
    userRepository.createUser(form.getUsername(), hash, true);
    return "redirect:/login";
  }
}
