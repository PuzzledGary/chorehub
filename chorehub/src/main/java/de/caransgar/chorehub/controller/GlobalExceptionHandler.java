package de.caransgar.chorehub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the ChoreHub API.
 * 
 * Handles common exceptions across all controllers and returns
 * consistent error response formats.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle HttpMessageNotReadableException (e.g., missing or invalid request
     * body).
     * 
     * @param ex the exception
     * @return error response with 400 status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Request body cannot be null";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            message = ex.getCause().getMessage();
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ChoreController.ErrorResponse(message));
    }

    /**
     * Handle IllegalArgumentException for business logic validation errors.
     * 
     * @param ex the exception
     * @return error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ChoreController.ErrorResponse(ex.getMessage()));
    }

}
