package com.cyberowl.snake.util;

import com.cyberowl.snake.constants.SnakeConstants;
import com.cyberowl.snake.model.Velocity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VelocityValidator implements ConstraintValidator<ValidVelocity, Velocity> {
    @Override
    public boolean isValid(Velocity velocity, ConstraintValidatorContext context) {
        if (velocity == null) {
            return false;
        }
        int x = velocity.velX();
        int y = velocity.velY();

        // x and y must be between -1 and 1, and (0,0) is not allowed
        return (x != 0 || y != 0) &&
                x >= SnakeConstants.MIN_VELOCITY && x <= SnakeConstants.MAX_VELOCITY &&
                y >= SnakeConstants.MIN_VELOCITY && y <= SnakeConstants.MAX_VELOCITY;
    }
}
