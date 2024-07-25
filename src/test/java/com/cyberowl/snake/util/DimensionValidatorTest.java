package com.cyberowl.snake.util;

import com.cyberowl.snake.constants.SnakeConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class DimensionValidatorTest {

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<DimensionsValidator> constructor = DimensionsValidator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = exception.getCause();
        assertInstanceOf(IllegalStateException.class, cause);
        assertEquals(SnakeConstants.UTILITY_CLASS_INSTANTIATION, cause.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, 0",
            "1, 1, 1",
            "10, 10, 0",
            "10, 10, 10",
            "100, 100, 0",
            "100, 100, 1",
            "1000, 1000, 0",
            "1000, 1000, 1"
    })
    void testValidDimensions(int width, int height, int start) {
        assertDoesNotThrow(() -> DimensionsValidator.validatePositiveDimensions(width, height, start));
    }

    @ParameterizedTest
    @CsvSource({
            "0, -1, 0",
            "0, -1, 1",
            "-1, 0, 0",
            "-1, 0, 1",
            "-1, -1, 0",
            "-1, -1, 1",
            "0, 0, 1",
            "1, -1, 0",
            "1, -1, 1",
            "-1, 1, 0",
            "-1, 1, 1"
    })
    void testInvalidDimensions(int width, int height, int start) {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                DimensionsValidator.validatePositiveDimensions(width, height, start)
        );

        String expectedMessage = String.format(SnakeConstants.INVALID_DIMENSIONS, width, height);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
