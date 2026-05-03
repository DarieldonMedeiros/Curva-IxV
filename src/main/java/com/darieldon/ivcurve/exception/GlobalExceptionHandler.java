package com.darieldon.ivcurve.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedVendorException.class)
    public ResponseEntity<ErrorResponse> handleUnsupported(UnsupportedVendorException ex, HttpServletRequest request) {

        return ResponseEntity.status(422).body(new ErrorResponse(422, "UNSUPPORTED_VENDOR", ex.getMessage(), ex.getFileName(), request.getRequestURI(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {

        return ResponseEntity.status(500).body(new ErrorResponse(500, "INTERNAL_ERROR", "Erro interno inesperado: " + ex.getMessage(), null, request.getRequestURI(), Instant.now()));
    }
}
