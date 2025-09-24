package de.jensknipper.re_director.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntInValidator implements ConstraintValidator<IntIn, Integer> {

  private Set<Integer> allowedValues;

  @Override
  public void initialize(IntIn constraintAnnotation) {
    allowedValues = IntStream.of(constraintAnnotation.value()).boxed().collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext context) {
    if (value == null || allowedValues.contains(value)) {
      return true;
    }
    String message = "must be one of " + allowedValues;
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    return false;
  }
}
