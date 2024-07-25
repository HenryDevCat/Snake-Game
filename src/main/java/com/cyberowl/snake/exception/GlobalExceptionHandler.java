package com.cyberowl.snake.exception;

import com.cyberowl.snake.constants.SnakeConstants;
import com.cyberowl.snake.util.CorrelationIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            methodArgumentNotValidException.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (ex instanceof ConstraintViolationException constraintViolationException) {
            constraintViolationException.getConstraintViolations().forEach(violation -> {
                String fieldName = violation.getPropertyPath().toString();
                String errorMessage = violation.getMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (ex instanceof MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
            handleMissingOrInvalidParameter(request, errors, methodArgumentTypeMismatchException.getName());
            checkOtherParameter(request, errors, methodArgumentTypeMismatchException.getName());
        } else if (ex instanceof MissingServletRequestParameterException missingServletRequestParameterException) {
            handleMissingOrInvalidParameter(request, errors, missingServletRequestParameterException.getParameterName());
            checkOtherParameter(request, errors, missingServletRequestParameterException.getParameterName());
        }
        return createErrorResponseEntity(HttpStatus.BAD_REQUEST, SnakeConstants.VALIDATION_ERROR_CD, ex, request,
                SnakeConstants.VALIDATION_ERROR, errors);
    }

    private void handleMissingOrInvalidParameter(HttpServletRequest request, Map<String, String> errors, String paramName) {
        String paramValue = request.getParameter(paramName);
        if (paramValue == null || paramValue.trim().isEmpty()) {
            errors.put(paramName, "Missing required parameter");
        } else {
            errors.put(paramName, "Invalid value for " + paramName);
        }
    }

    private void checkOtherParameter(HttpServletRequest request, Map<String, String> errors, String exceptionParamName) {
        String otherParamName = exceptionParamName.equals("w") ? "h" : "w";
        String otherParamValue = request.getParameter(otherParamName);
        if (otherParamValue == null || otherParamValue.trim().isEmpty()) {
            errors.put(otherParamName, "Missing required parameter");
        } else {
            errors.put(otherParamName, "Invalid value for " + otherParamName);
        }
    }

    @ExceptionHandler(FruitNotReachedException.class)
    public ResponseEntity<ErrorResponse> handleFruitNotReachedException(FruitNotReachedException ex, HttpServletRequest request) {
        return createErrorResponseEntity(HttpStatus.NOT_FOUND, SnakeConstants.FRUIT_NOT_REACHED_CD, ex, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return createErrorResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, SnakeConstants.METHOD_NOT_ALLOWED_CD, ex, request,
                SnakeConstants.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(GameOverException.class)
    public ResponseEntity<ErrorResponse> handleGameOverException(GameOverException ex, HttpServletRequest request) {
        return createErrorResponseEntity(HttpStatus.I_AM_A_TEAPOT, SnakeConstants.GAME_OVER_CD, ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return createErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, SnakeConstants.INTERNAL_SERVER_ERROR_CD, ex, request,
                SnakeConstants.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status, String errorCode, Exception ex, HttpServletRequest request) {
        return createErrorResponseEntity(status, errorCode, ex, request, ex.getMessage(), null);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status, String errorCode, Exception ex, HttpServletRequest request, String clientMessage) {
        return createErrorResponseEntity(status, errorCode, ex, request, clientMessage, null);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status, String errorCode, Exception ex, HttpServletRequest request, String clientMessage, Map<String, String> details) {
        String correlationId = CorrelationIdUtil.getOrGenerateCorrelationId(
                request.getHeader(SnakeConstants.CORRELATION_ID_HEADER)
        );

        logException(status, errorCode, ex, correlationId);

        ErrorResponse errorResponse = new ErrorResponse(correlationId,
                status.value(),
                errorCode,
                clientMessage,
                request.getRequestURI(),
                Instant.now(),
                details
        );

        return ResponseEntity.status(status)
                .header(SnakeConstants.CORRELATION_ID_HEADER, correlationId)
                .body(errorResponse);
    }

    private void logException(HttpStatus status, String errorCode, Exception ex, String correlationId) {
        StringBuilder logMessage = new StringBuilder()
                .append("CorrelationID: ").append(correlationId)
                .append(" | Status: ").append(status)
                .append(" | ErrorCode: ").append(errorCode)
                .append(" | Exception: ").append(ex.getMessage());

        if (status.is4xxClientError()) {
            log.warn(logMessage.toString(), ex);
        } else {
            log.error(logMessage.toString(), ex);
        }
    }
}
