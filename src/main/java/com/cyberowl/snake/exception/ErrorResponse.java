package com.cyberowl.snake.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(String correlationId, int status, String errorCode, String message, String path,
                            Instant timestamp, Map<String, String> details) {
}
