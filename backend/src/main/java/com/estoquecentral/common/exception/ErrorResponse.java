package com.estoquecentral.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response format (Story 8.5 - AC1).
 *
 * Provides consistent error structure across all endpoints.
 *
 * @param timestamp When the error occurred
 * @param status HTTP status code
 * @param error Short error description
 * @param message Detailed error message
 * @param path Request path that caused the error
 * @param fieldErrors Field-specific validation errors (only for validation failures)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> fieldErrors
) {}
