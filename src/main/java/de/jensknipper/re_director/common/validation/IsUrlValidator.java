package de.jensknipper.re_director.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class IsUrlValidator implements ConstraintValidator<IsUrl, String> {

  private static final List<String> ALLOWED_SCHEMES = List.of("http", "https");

  @Override
  public boolean isValid(@Nullable String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    try {
      URL url = new URI(value).toURL();
      return ALLOWED_SCHEMES.contains(url.getProtocol());
    } catch (MalformedURLException | URISyntaxException | IllegalArgumentException _) {
      return false;
    }
  }
}
