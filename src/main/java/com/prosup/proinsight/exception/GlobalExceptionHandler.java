package com.prosup.proinsight.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.*;

/**
 * Centralized exception handler that returns RFC7807 Problem Details JSON.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType PROBLEM_JSON = MediaType.parseMediaType("application/problem+json");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> violations = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> {
            Map<String, String> v = new HashMap<>();
            v.put("field", fe.getField());
            v.put("message", fe.getDefaultMessage());
            violations.add(v);
        });

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://example.com/problems/validation-error");
        body.put("title", "Validation Failed");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("detail", "Um ou mais campos estão inválidos. Veja 'violations' para detalhes.");
        body.put("instance", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());
        body.put("violations", violations);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<Map<String, String>> violations = new ArrayList<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            Map<String, String> v = new HashMap<>();
            String path = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "";
            v.put("field", path);
            v.put("message", cv.getMessage());
            violations.add(v);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://example.com/problems/constraint-violation");
        body.put("title", "Constraint Violation");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("detail", "Validation constraint violated");
        body.put("instance", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());
        body.put("violations", violations);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://example.com/problems/data-integrity");
        body.put("title", "Data integrity violation");
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("detail", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        body.put("instance", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex, HttpServletRequest request, WebRequest webRequest) {
        // for unexpected errors, do not leak internal details in production
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://example.com/problems/internal-server-error");
        body.put("title", "Internal Server Error");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("detail", "An unexpected error occurred. Use traceId to find details in logs.");
        body.put("instance", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());
        body.put("traceId", traceId);

        // log exception with traceId (assumes a logger is configured)
        ex.printStackTrace();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

