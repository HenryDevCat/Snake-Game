package com.cyberowl.snake.exception;

public class GameOverException extends RuntimeException {
    public GameOverException(String message) {
        super(message);
    }
}
