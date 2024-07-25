package com.cyberowl.snake.constants;

import com.cyberowl.snake.model.Coordinate;
import com.cyberowl.snake.model.Velocity;

public class SnakeConstants {
    private SnakeConstants() {
        // Private constructor to prevent instantiation
    }

    // Initialization
    public static final int INITIAL_SCORE = 0;
    public static final Coordinate INITIAL_SNAKE_POSITION = new Coordinate(0, 0);
    public static final Velocity INITIAL_SNAKE_VELOCITY = new Velocity(1, 0);

    // Configurations
    public static final int MIN_VELOCITY = -1;
    public static final int MAX_VELOCITY = 1;
    public static final int POINTS_PER_FRUIT = 1;

    // Generics
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String UTILITY_CLASS_INSTANTIATION = "This is a utility class and should not be instantiated.";

    // Error code
    public static final String VALIDATION_ERROR_CD = "VALIDATION_ERROR";
    public static final String FRUIT_NOT_REACHED_CD = "FRUIT_NOT_REACHED";
    public static final String METHOD_NOT_ALLOWED_CD = "METHOD_NOT_ALLOWED";
    public static final String GAME_OVER_CD = "GAME_OVER";
    public static final String INTERNAL_SERVER_ERROR_CD = "INTERNAL_SERVER_ERROR";

    // Error messages
    public static final String VALIDATION_ERROR = "Invalid input parameters";
    public static final String METHOD_NOT_ALLOWED = "The requested method is not allowed for this endpoint.";
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later.";
    public static final String REVERSE_DIRECTION = "Snake cannot move in reverse direction.";
    public static final String OUT_OF_BOUNDS = "Snake moved out of bounds.";
    public static final String GAME_OVER = "Game is over, snake went out of bounds or made an invalid move.";
    public static final String FRUIT_NOT_REACHED = "Fruit not found, the ticks do not lead the snake to the fruit position.";
    public static final String INVALID_COORDINATE = "Invalid coordinate: x and y must be non-negative integers.";
    public static final String INVALID_DIMENSIONS = "Width and height must be positive integers. Provided width = %d, height = %d";
    public static final String INVALID_VELOCITY = "Invalid velocity: x and y must be between -1 and 1, and (0,0) is not allowed";
}
