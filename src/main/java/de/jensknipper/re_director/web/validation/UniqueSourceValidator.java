package de.jensknipper.re_director.web.validation;

import de.jensknipper.re_director.db.RedirectRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueSourceValidator implements ConstraintValidator<UniqueSource, String> {

  private final RedirectRepository redirectRepository;

  public UniqueSourceValidator(RedirectRepository redirectRepository) {
    this.redirectRepository = redirectRepository;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return !redirectRepository.redirectAlreadyExists(value);
  }
}
