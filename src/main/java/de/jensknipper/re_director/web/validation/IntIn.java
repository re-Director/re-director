package de.jensknipper.re_director.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IntInValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntIn {
  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  int[] value();
}
