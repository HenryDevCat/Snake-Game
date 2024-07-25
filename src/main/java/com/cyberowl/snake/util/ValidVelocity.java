package com.cyberowl.snake.util;

import com.cyberowl.snake.constants.SnakeConstants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = VelocityValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVelocity {
    String message() default SnakeConstants.INVALID_VELOCITY;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
