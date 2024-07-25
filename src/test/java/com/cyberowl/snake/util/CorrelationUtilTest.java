package com.cyberowl.snake.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationUtilTest {

    @Test
    void testGetOrGenerateCorrelationId() {
        // Test with null input
        String generatedIdFromNull = CorrelationIdUtil.getOrGenerateCorrelationId(null);
        assertNotNull(generatedIdFromNull);
        assertTrue(isValidUUID(generatedIdFromNull));

        // Test with empty string input
        String generatedIdFromEmptyString = CorrelationIdUtil.getOrGenerateCorrelationId("");
        assertNotNull(generatedIdFromEmptyString);
        assertTrue(isValidUUID(generatedIdFromEmptyString));

        // Test with non-empty string input
        String existingId = UUID.randomUUID().toString();
        String returnedExistingId = CorrelationIdUtil.getOrGenerateCorrelationId(existingId);
        assertEquals(existingId, returnedExistingId);
        assertTrue(isValidUUID(returnedExistingId));
    }

    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
