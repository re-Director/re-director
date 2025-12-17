package de.jensknipper.re_director.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class IsUrlValidator implements ConstraintValidator<IsUrl, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      new URI(value).toURL();
    } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
      return false;
    }
    return true;
  }
}
