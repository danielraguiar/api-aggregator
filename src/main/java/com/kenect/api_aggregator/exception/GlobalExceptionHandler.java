package com.kenect.api_aggregator.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Map<String, Object>> handleExternalApiException(ExternalApiException ex) {
        log.error("External API error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "Failed to retrieve data from external service",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("Validation error: {}", details);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameters",
                details
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        String details = ex.getAllErrors().stream()
                .map(error -> {
                    String defaultMessage = error.getDefaultMessage();
                    if (defaultMessage == null) {
                        return "Validation failed";
                    }
                    
                    if (defaultMessage.contains("Failed to convert property value")) {
                        return extractUserFriendlyMessage(defaultMessage);
                    }
                    
                    return defaultMessage;
                })
                .collect(Collectors.joining(", "));
        
        log.warn("Validation error: {}", details);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameters",
                details
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String defaultMessage = error.getDefaultMessage();
                    if (defaultMessage != null && defaultMessage.contains("Failed to convert property value")) {
                        return extractUserFriendlyMessage(defaultMessage);
                    }
                    return defaultMessage != null ? defaultMessage : "Validation failed";
                })
                .collect(Collectors.joining(", "));
        
        log.warn("Validation error: {}", details);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameters",
                details
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String providedValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        
        String details;
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            Object[] enumConstants = ex.getRequiredType().getEnumConstants();
            String validValues = enumConstants != null ? 
                    java.util.Arrays.stream(enumConstants)
                            .map(Object::toString)
                            .collect(Collectors.joining(", ")) : "";
            
            details = String.format(
                    "Invalid value '%s' for parameter '%s'. Valid values are: %s",
                    providedValue, paramName, validValues
            );
        } else {
            details = String.format(
                    "Invalid value '%s' for parameter '%s'. Expected type: %s",
                    providedValue, paramName, requiredType
            );
        }
        
        log.warn("Type mismatch error: {}", details);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter type",
                details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Message not readable: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed request",
                "Request body is not readable or contains invalid data"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameters",
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                ex.getMessage()
        );
    }

    private String extractUserFriendlyMessage(String errorMessage) {
        try {
            if (errorMessage.contains("ContactSource")) {
                String paramName = extractParameterName(errorMessage);
                String invalidValue = extractInvalidValue(errorMessage);
                return String.format(
                        "Invalid value '%s' for parameter '%s'. Valid values are: KENECT_LABS",
                        invalidValue, paramName
                );
            }
            
            if (errorMessage.contains("Integer")) {
                String paramName = extractParameterName(errorMessage);
                String invalidValue = extractInvalidValue(errorMessage);
                return String.format(
                        "Invalid value '%s' for parameter '%s'. Expected a number",
                        invalidValue, paramName
                );
            }
            
            String paramName = extractParameterName(errorMessage);
            if (!paramName.isEmpty()) {
                return String.format("Invalid value for parameter '%s'", paramName);
            }
        } catch (Exception e) {
            log.debug("Could not extract user-friendly message from: {}", errorMessage);
        }
        
        return "Invalid parameter value";
    }
    
    private String extractParameterName(String errorMessage) {
        int propertyIndex = errorMessage.indexOf("for property '");
        if (propertyIndex != -1) {
            int startIndex = propertyIndex + 14;
            int endIndex = errorMessage.indexOf("'", startIndex);
            if (endIndex != -1) {
                return errorMessage.substring(startIndex, endIndex);
            }
        }
        return "";
    }
    
    private String extractInvalidValue(String errorMessage) {
        int valueIndex = errorMessage.indexOf("for value [");
        if (valueIndex != -1) {
            int startIndex = valueIndex + 11;
            int endIndex = errorMessage.indexOf("]", startIndex);
            if (endIndex != -1) {
                return errorMessage.substring(startIndex, endIndex);
            }
        }
        
        int stringIndex = errorMessage.indexOf("For input string: \"");
        if (stringIndex != -1) {
            int startIndex = stringIndex + 19;
            int endIndex = errorMessage.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return errorMessage.substring(startIndex, endIndex);
            }
        }
        
        return "unknown";
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message, String details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("details", details);
        return new ResponseEntity<>(body, status);
    }
}
