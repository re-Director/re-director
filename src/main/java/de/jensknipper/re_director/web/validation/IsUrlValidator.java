package de.jensknipper.re_director.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.jspecify.annotations.Nullable;

public class IsUrlValidator implements ConstraintValidator<IsUrl, String> {

  @Override
  public boolean isValid(@Nullable String value, ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }
    try {
      new URI(value).toURL();
    } catch (MalformedURLException | URISyntaxException | IllegalArgumentException _) {
      return false;
    }
    return true;
  }
}
