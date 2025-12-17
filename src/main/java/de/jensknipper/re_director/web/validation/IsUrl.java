package de.jensknipper.re_director.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsUrlValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsUrl {
  String message() default "should be a URL";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
