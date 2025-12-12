package de.jensknipper.re_director.web.validation;

import de.jensknipper.re_director.service.RedirectService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueSourceValidator implements ConstraintValidator<UniqueSource, String> {

  private final RedirectService redirectService;

  public UniqueSourceValidator(RedirectService redirectService) {
    this.redirectService = redirectService;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return !redirectService.redirectAlreadyExists(value);
  }
}
