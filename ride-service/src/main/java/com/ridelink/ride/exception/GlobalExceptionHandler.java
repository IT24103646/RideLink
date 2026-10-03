package com.ridelink.ride.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ridelink.ride.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        String message = errors.isEmpty() ? "Validation failed" : errors.values().iterator().next();
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Request body is invalid."));
    }

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRideNotFound(RideNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("RIDE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(InvalidRideTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidRideTransitionException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("INVALID_RIDE_STATUS_TRANSITION", exception.getMessage()));
    }

    @ExceptionHandler(NoAvailableDriverException.class)
    public ResponseEntity<ErrorResponse> handleNoAvailableDriver(NoAvailableDriverException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse("NO_AVAILABLE_DRIVER", exception.getMessage()));
    }

    @ExceptionHandler(DriverServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleDriverServiceUnavailable(DriverServiceUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse("DRIVER_SERVICE_UNAVAILABLE", "A driver cannot be assigned now. Please retry."));
    }

    @ExceptionHandler(DriverServiceAuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleDriverServiceAuthorization(DriverServiceAuthorizationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("DRIVER_SERVICE_UNAUTHORIZED", "Driver assignment authorization was rejected."));
    }

    @ExceptionHandler(ForbiddenRideAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAccess(ForbiddenRideAccessException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("FORBIDDEN_RIDE_ACCESS", exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("FORBIDDEN_RIDE_ACCESS", "You are not allowed to access this ride."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred."));
    }
}
