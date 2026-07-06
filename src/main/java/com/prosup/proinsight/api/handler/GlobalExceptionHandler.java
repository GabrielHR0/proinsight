package com.prosup.proinsight.api.handler;

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

import java.time.Instant;
import java.util.*;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "https://example.com/problems/bad-request",
            "Bad Request",
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://example.com/problems/internal-server-error");
        body.put("title", "Internal Server Error");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("detail", "Ocorreu um erro inesperado. Use o traceId para rastrear.");
        body.put("instance", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());
        body.put("traceId", traceId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> construirErroRFC7807(
        String type,
        String title,
        int status,
        String detail,
        String instance
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", type);
        body.put("title", title);
        body.put("status", status);
        body.put("detail", detail);
        body.put("instance", instance);
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
