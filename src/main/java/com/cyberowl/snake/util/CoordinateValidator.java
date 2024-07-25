package com.cyberowl.snake.util;

import com.cyberowl.snake.model.Coordinate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CoordinateValidator implements ConstraintValidator<ValidCoordinate, Coordinate> {
    @Override
    public boolean isValid(Coordinate coordinate, ConstraintValidatorContext context) {
        if (coordinate == null) {
            return false;
        }
        int x = coordinate.x();
        int y = coordinate.y();

        // x and y must be non-negative integers
        return x >= 0 && y >= 0;
    }
}
