package com.cyberowl.snake.exception;

import com.cyberowl.snake.constants.SnakeConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    BindingResult bindingResult;

    @Mock
    ConstraintViolation<?> constraintViolation;

    @Mock
    Path path;

    @BeforeEach
    void setUp() throws Exception {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            exceptionHandler = new GlobalExceptionHandler();
            when(mockRequest.getRequestURI()).thenReturn("/test");
        }
    }

    private void assertErrorResponse(ResponseEntity<ErrorResponse> response, HttpStatus expectedStatus, String expectedErrorCode) {
        assertEquals(expectedStatus.value(), response.getStatusCode().value());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.correlationId());
        assertEquals(expectedStatus.value(), errorResponse.status());
        assertEquals(expectedErrorCode, errorResponse.errorCode());
        assertNotNull(errorResponse.message());
        assertEquals("/test", errorResponse.path());
        assertNotNull(errorResponse.timestamp());
        assertTrue(errorResponse.timestamp().isBefore(Instant.now()) || errorResponse.timestamp().equals(Instant.now()));
    }

    @Nested
    class ValidationExceptionTests {
        @Test
        void handleMethodArgumentNotValidException() {
            FieldError fieldError = new FieldError("object", "field", "defaultMessage");
            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(methodArgumentNotValidException, mockRequest);

            assertErrorResponse(response, HttpStatus.BAD_REQUEST, SnakeConstants.VALIDATION_ERROR_CD);
            assertEquals("defaultMessage", Objects.requireNonNull(response.getBody()).details().get("field"));
        }

        @Test
        void handleConstraintViolationException() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            violations.add(constraintViolation);
            ConstraintViolationException ex = new ConstraintViolationException(violations);
            when(constraintViolation.getPropertyPath()).thenReturn(path);
            when(constraintViolation.getMessage()).thenReturn("error message");

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, mockRequest);

            assertErrorResponse(response, HttpStatus.BAD_REQUEST, SnakeConstants.VALIDATION_ERROR_CD);
            assertNotNull(Objects.requireNonNull(response.getBody()).details());
        }

        @ParameterizedTest
        @MethodSource("provideArgumentsForMethodArgumentTypeMismatchException")
        void handleMethodArgumentTypeMismatchException(String exceptionParam, String wValue, String hValue, String expectedWMessage, String expectedHMessage) {
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", String.class, exceptionParam, null, null);
            when(mockRequest.getParameter("w")).thenReturn(wValue);
            when(mockRequest.getParameter("h")).thenReturn(hValue);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, mockRequest);

            assertErrorResponse(response, HttpStatus.BAD_REQUEST, SnakeConstants.VALIDATION_ERROR_CD);
            Map<String, String> details = Objects.requireNonNull(response.getBody()).details();
            assertNotNull(details);
            assertEquals(expectedWMessage, details.get("w"));
            assertEquals(expectedHMessage, details.get("h"));
        }

        private static Stream<Arguments> provideArgumentsForMethodArgumentTypeMismatchException() {
            return Stream.of(
                    Arguments.of("w", "abc", "def", "Invalid value for w", "Invalid value for h"),
                    Arguments.of("w", "abc", null, "Invalid value for w", "Missing required parameter"),
                    Arguments.of("w", "abc", " ", "Invalid value for w", "Missing required parameter"),
                    Arguments.of("w", "abc", "", "Invalid value for w", "Missing required parameter"),
                    Arguments.of("h", "def", "abc", "Invalid value for w", "Invalid value for h"),
                    Arguments.of("h", null, "abc", "Missing required parameter", "Invalid value for h"),
                    Arguments.of("h", " ", "abc", "Missing required parameter", "Invalid value for h"),
                    Arguments.of("h", "", "abc", "Missing required parameter", "Invalid value for h")
            );
        }

        @ParameterizedTest
        @MethodSource("provideArgumentsForMissingServletRequestParameterException")
        void handleMissingServletRequestParameterException(String missingParam, String wValue, String hValue, String expectedWMessage, String expectedHMessage) {
            MissingServletRequestParameterException ex = new MissingServletRequestParameterException(missingParam, "int");
            when(mockRequest.getParameter("w")).thenReturn(wValue);
            when(mockRequest.getParameter("h")).thenReturn(hValue);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, mockRequest);

            assertErrorResponse(response, HttpStatus.BAD_REQUEST, SnakeConstants.VALIDATION_ERROR_CD);
            Map<String, String> details = Objects.requireNonNull(response.getBody()).details();
            assertNotNull(details);
            assertEquals(expectedWMessage, details.get("w"));
            assertEquals(expectedHMessage, details.get("h"));
        }

        private static Stream<Arguments> provideArgumentsForMissingServletRequestParameterException() {
            return Stream.of(
                    Arguments.of("w", null, "def", "Missing required parameter", "Invalid value for h"),
                    Arguments.of("w", "", "def", "Missing required parameter", "Invalid value for h"),
                    Arguments.of("w", " ", "def", "Missing required parameter", "Invalid value for h"),
                    Arguments.of("h", "abc", null, "Invalid value for w", "Missing required parameter"),
                    Arguments.of("h", "abc", "", "Invalid value for w", "Missing required parameter"),
                    Arguments.of("h", "abc", " ", "Invalid value for w", "Missing required parameter")
            );
        }
    }

    @Nested
    class CustomExceptionTests {
        @Test
        void handleFruitNotReachedException() {
            FruitNotReachedException ex = new FruitNotReachedException(SnakeConstants.FRUIT_NOT_REACHED);
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleFruitNotReachedException(ex, mockRequest);

            assertErrorResponse(response, HttpStatus.NOT_FOUND, SnakeConstants.FRUIT_NOT_REACHED_CD);
        }

        @Test
        void handleGameOverException() {
            GameOverException ex = new GameOverException(SnakeConstants.GAME_OVER);
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGameOverException(ex, mockRequest);

            assertErrorResponse(response, HttpStatus.I_AM_A_TEAPOT, SnakeConstants.GAME_OVER_CD);
        }
    }

    @Test
    void handleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotAllowed(ex, mockRequest);

        assertErrorResponse(response, HttpStatus.METHOD_NOT_ALLOWED, SnakeConstants.METHOD_NOT_ALLOWED_CD);
    }

    @Test
    void handleGenericException() {
        Exception ex = new RuntimeException(SnakeConstants.INTERNAL_SERVER_ERROR);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, mockRequest);

        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, SnakeConstants.INTERNAL_SERVER_ERROR_CD);
    }

    @Nested
    class CorrelationIdTests {
        @Test
        void shouldUseProvidedCorrelationId() {
            String providedCorrelationId = UUID.randomUUID().toString();
            when(mockRequest.getHeader(SnakeConstants.CORRELATION_ID_HEADER)).thenReturn(providedCorrelationId);

            Exception ex = new RuntimeException(SnakeConstants.INTERNAL_SERVER_ERROR);
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, mockRequest);

            assertNotNull(response.getHeaders().get(SnakeConstants.CORRELATION_ID_HEADER));
            assertEquals(providedCorrelationId, Objects.requireNonNull(response.getHeaders().get(SnakeConstants.CORRELATION_ID_HEADER)).get(0));
            assertEquals(providedCorrelationId, Objects.requireNonNull(response.getBody()).correlationId());
        }

        @Test
        void shouldGenerateCorrelationIdIfNotProvided() {
            when(mockRequest.getHeader(SnakeConstants.CORRELATION_ID_HEADER)).thenReturn(null);

            Exception ex = new RuntimeException(SnakeConstants.INTERNAL_SERVER_ERROR);
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, mockRequest);

            assertNotNull(response.getHeaders().get(SnakeConstants.CORRELATION_ID_HEADER));
            assertNotNull(Objects.requireNonNull(response.getHeaders().get(SnakeConstants.CORRELATION_ID_HEADER)).get(0));
            assertNotNull(Objects.requireNonNull(response.getBody()).correlationId());
            assertEquals(Objects.requireNonNull(response.getHeaders().get(SnakeConstants.CORRELATION_ID_HEADER)).get(0), response.getBody().correlationId());
        }
    }
}
