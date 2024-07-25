package com.cyberowl.snake.util;

import com.cyberowl.snake.model.Coordinate;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoordinateValidatorTest {
    private CoordinateValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new CoordinateValidator();
        context = null;
    }

    @Test
    void testNullCoordinate() {
        assertFalse(validator.isValid(null, context));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "0, 1",
            "1, 0",
            "1, 1",
            "0, 10",
            "10, 0",
            "10, 10",
            "0, 100",
            "100, 0",
            "100, 100",
            "0, 1000",
            "1000, 0",
            "1000, 1000"
    })
    void testValidCoordinates(int x, int y) {
        Coordinate coordinate = new Coordinate(x, y);
        assertTrue(validator.isValid(coordinate, context));
    }

    @ParameterizedTest
    @CsvSource({
            "0, -1",
            "-1, 0",
            "-1, -1"
    })
    void testInvalidCoordinates(int x, int y) {
        Coordinate coordinate = new Coordinate(x, y);
        assertFalse(validator.isValid(coordinate, context));
    }
}
