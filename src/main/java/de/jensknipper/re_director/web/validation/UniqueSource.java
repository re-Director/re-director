package de.jensknipper.re_director.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueSourceValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueSource {
  String message() default "source is not unique";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
