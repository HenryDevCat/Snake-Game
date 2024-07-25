package com.cyberowl.snake.util;

import com.cyberowl.snake.constants.SnakeConstants;

public class DimensionsValidator {
    private DimensionsValidator() {
        throw new IllegalStateException(SnakeConstants.UTILITY_CLASS_INSTANTIATION);
    }

    public static void validatePositiveDimensions(int width, int height, int start) {
        if (width < start || height < start) {
            throw new IllegalArgumentException(String.format(SnakeConstants.INVALID_DIMENSIONS, width, height));
        }
    }
}
