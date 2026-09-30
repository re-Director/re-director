package de.jensknipper.redirector.auth;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class PasswordValidationService {

  private final PasswordRulesProperties passwordRulesProperties;

  public PasswordValidationService(PasswordRulesProperties passwordRulesProperties) {
    this.passwordRulesProperties = passwordRulesProperties;
  }

  public void validatePasswordMismatch(
      BindingResult bindingResult, @Nullable String password, @Nullable String confirmPassword) {
    if (password == null || !password.equals(confirmPassword)) {
      bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
    }
  }

  public void validatePassword(BindingResult bindingResult, @Nullable String password) {
    if (password == null || password.length() < passwordRulesProperties.minLength()) {
      bindingResult.rejectValue(
          "password",
          "password.min-length",
          "Passwords should be at least " + passwordRulesProperties.minLength() + " characters");
    }
  }
}
