package com.cyberowl.snake.util;

import com.cyberowl.snake.constants.SnakeConstants;
import com.cyberowl.snake.model.Velocity;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelocityValidatorTest {
    private VelocityValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new VelocityValidator();
        context = null;
    }

    @Test
    void testNullVelocity() {
        assertFalse(validator.isValid(null, context));
    }

    @ParameterizedTest
    @CsvSource({
            "-1, -1",
            "-1, 0",
            "0, -1",
            "0, 1",
            "1, -1",
            "1, 0",
            "-1, 1",
            "1, 1"
    })
    void testValidVelocities(int x, int y) {
        Velocity velocity = new Velocity(x, y);
        assertTrue(validator.isValid(velocity, context));
    }

    @ParameterizedTest
    @CsvSource({
            "-2, -2",
            "-2, 0",
            "0, -2",
            "0, 0",
            "0, 2",
            "2, 0",
            "2, 2"
    })
    void testInvalidVelocities(int x, int y) {
        Velocity velocity = new Velocity(x, y);
        assertFalse(validator.isValid(velocity, context));
    }

    @Test
    void testVelocityBoundaries() {
        assertTrue(validator.isValid(new Velocity(SnakeConstants.MIN_VELOCITY, 0), context));
        assertTrue(validator.isValid(new Velocity(SnakeConstants.MAX_VELOCITY, 0), context));
        assertTrue(validator.isValid(new Velocity(0, SnakeConstants.MIN_VELOCITY), context));
        assertTrue(validator.isValid(new Velocity(0, SnakeConstants.MAX_VELOCITY), context));

        assertFalse(validator.isValid(new Velocity(SnakeConstants.MIN_VELOCITY - 1, 0), context));
        assertFalse(validator.isValid(new Velocity(SnakeConstants.MAX_VELOCITY + 1, 0), context));
        assertFalse(validator.isValid(new Velocity(0, SnakeConstants.MIN_VELOCITY - 1), context));
        assertFalse(validator.isValid(new Velocity(0, SnakeConstants.MAX_VELOCITY + 1), context));
    }
}
