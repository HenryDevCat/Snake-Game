package com.cyberowl.snake.util;

import java.util.UUID;

public class CorrelationIdUtil {
    private CorrelationIdUtil() {
        // Private constructor to prevent instantiation
    }

    public static String getOrGenerateCorrelationId(String correlationId) {
        return (correlationId != null && !correlationId.isEmpty()) ? correlationId : UUID.randomUUID().toString();
    }
}
