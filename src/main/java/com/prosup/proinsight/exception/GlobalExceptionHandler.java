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
    
    /**
     * Trata ValidacaoException - campos obrigatórios, tamanho, etc.
     * 400 Bad Request: Entrada inválida
     */
    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<Object> handleValidacaoException(ValidacaoException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "https://example.com/problems/validation-error",
            "Validation Failed",
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Trata RegraNeggocioException - pré-requisitos, frequência cardíaca, etc.
     * 422 Unprocessable Entity: Semântica inválida (regra de negócio)
     */
    @ExceptionHandler(RegraNeggocioException.class)
    public ResponseEntity<Object> handleRegraNeggocioException(RegraNeggocioException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "https://example.com/problems/business-rule-violation",
            "Business Rule Violation",
            422,
            ex.getMessage(),
            request.getRequestURI()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    /**
     * Trata RecursoNaoEncontradoException - tabela, cliente, avaliador, etc.
     * 404 Not Found: Recurso não existe
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> handleRecursoNaoEncontradoException(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "https://example.com/problems/resource-not-found",
            "Resource Not Found",
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Trata AvaliacaoException - erros no processamento da avaliação.
     * 500 Internal Server Error: Erro no servidor
     */
    @ExceptionHandler(AvaliacaoException.class)
    public ResponseEntity<Object> handleAvaliacaoException(AvaliacaoException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> body = construirErroRFC7807(
            "https://example.com/problems/avaliacao-error",
            "Evaluation Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Erro ao processar avaliação. traceId: " + traceId,
            request.getRequestURI()
        );
        body.put("traceId", traceId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

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
    
    /**
     * Constrói resposta RFC7807 (Problem Details).
     * Formato padrão para respostas de erro em APIs REST.
     */
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


