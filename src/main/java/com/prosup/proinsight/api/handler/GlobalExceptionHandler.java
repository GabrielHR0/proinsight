package com.prosup.proinsight.api.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType PROBLEM_JSON = MediaType.parseMediaType("application/problem+json");
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        body.put("type", "proinsight://problems/validation-error");
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
        body.put("type", "proinsight://problems/constraint-violation");
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
        log.warn("Data integrity violation at URI: {}", request.getRequestURI());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "proinsight://problems/data-integrity");
        body.put("title", "Data integrity violation");
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("detail", "Conflito ao salvar dados. Verifique se já existe um registro com os mesmos dados.");
        body.put("instance", request.getRequestURI());
        body.put("timestamp", Instant.now().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad request [{}]: URI={}, detalhe={}",
                request.getRemoteAddr(), request.getRequestURI(), ex.getMessage());

        String detail = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "Requisição inválida.";

        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/bad-request",
            "Bad Request",
            HttpStatus.BAD_REQUEST.value(),
            detail,
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        log.warn("Recurso não encontrado [{}]: URI={}, detalhe={}",
                request.getRemoteAddr(), request.getRequestURI(), ex.getMessage());

        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/not-found",
            "Not Found",
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "Recurso não encontrado.",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Estado inválido [{}]: URI={}, detalhe={}",
                request.getRemoteAddr(), request.getRequestURI(), ex.getMessage());

        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/unprocessable-entity",
            "Unprocessable Entity",
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "Não foi possível concluir a operação.",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Object> handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
        log.warn("Rate limit excedido: URI={}, IP={}", request.getRequestURI(), request.getRemoteAddr());

        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/too-many-requests",
            "Too Many Requests",
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Muitas tentativas. Tente novamente em 1 minuto.",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Object> handleAuthentication(Exception ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/unauthorized",
            "Unauthorized",
            HttpStatus.UNAUTHORIZED.value(),
            "Credenciais inválidas",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userInfo = "anonymous";
        Collection<String> authorities = Collections.emptyList();
        if (auth != null && auth.getPrincipal() instanceof com.prosup.proinsight.domain.model.CustomUserDetails cud) {
            userInfo = "userId=" + cud.getUser().getId() + ", email=" + cud.getUser().getEmail();
            authorities = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList();
        }
        log.warn("[403] Acesso negado: URI={}, IP={}, user={}, authorities={}, motivo={}",
                request.getRequestURI(), request.getRemoteAddr(), userInfo, authorities, ex.getMessage());

        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/forbidden",
            "Forbidden",
            HttpStatus.FORBIDDEN.value(),
            "Acesso negado. Você não tem permissão para executar esta operação.",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Object> handleLocked(LockedException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/locked",
            "Account Locked",
            HttpStatus.TOO_MANY_REQUESTS.value(),
            ex.getMessage(),
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/unauthorized",
            "Unauthorized",
            HttpStatus.UNAUTHORIZED.value(),
            "Autenticação falhou",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/malformed-json",
            "Malformed JSON",
            HttpStatus.BAD_REQUEST.value(),
            "Corpo da requisição inválido. Verifique o formato JSON.",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/unsupported-media-type",
            "Unsupported Media Type",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            "Tipo de mídia não suportado. Use application/json.",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        Map<String, Object> body = construirErroRFC7807(
            "proinsight://problems/type-mismatch",
            "Type Mismatch",
            HttpStatus.BAD_REQUEST.value(),
            "Parâmetro com valor inválido",
            request.getRequestURI()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();

        log.error("Erro interno [{}] - URI: {}", traceId, request.getRequestURI(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "proinsight://problems/internal-server-error");
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
