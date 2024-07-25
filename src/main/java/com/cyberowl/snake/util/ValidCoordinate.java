package com.cyberowl.snake.util;

import com.cyberowl.snake.constants.SnakeConstants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CoordinateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCoordinate {
    String message() default SnakeConstants.INVALID_COORDINATE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
