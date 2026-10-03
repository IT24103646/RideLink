package com.ridelink.account.exception;

import com.ridelink.account.dto.ErrorResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ErrorResponse> duplicate(DuplicateAccountException exception) {
        return error(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> invalidCredentials(InvalidCredentialsException exception) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(AccountNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> invalidRole(InvalidRoleException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ROLE", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorResponse> invalidStatus(InvalidStatusException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_STATUS", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ForbiddenAccountAccessException.class)
    public ResponseEntity<ErrorResponse> forbidden(ForbiddenAccountAccessException exception) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN_ACCOUNT_ACCESS", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AccountSuspendedException.class)
    public ResponseEntity<ErrorResponse> suspended(AccountSuspendedException exception) {
        return error(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> disabled(AccountDisabledException exception) {
        return error(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN_ACCOUNT_ACCESS", "You are not allowed to access this account.", Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError -> errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolation(ConstraintViolationException exception) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException exception) {
        String code = exception.getMessage() != null && exception.getMessage().contains("role")
                ? "INVALID_ROLE" : "VALIDATION_ERROR";
        return error(HttpStatus.BAD_REQUEST, code, "Request body is invalid", Map.of());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message,
                                                Map<String, String> validationErrors) {
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), code, message, validationErrors));
    }
}