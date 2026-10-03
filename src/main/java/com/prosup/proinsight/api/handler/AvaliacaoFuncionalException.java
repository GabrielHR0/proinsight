package com.prosup.proinsight.api.handler;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AvaliacaoFuncionalException extends RuntimeException {

    private final HttpStatus status;
    private final List<Map<String, String>> violations;

    public AvaliacaoFuncionalException(HttpStatus status, String message, List<Map<String, String>> violations) {
        super(message);
        this.status = status;
        this.violations = violations != null ? Collections.unmodifiableList(violations) : Collections.emptyList();
    }

    public static AvaliacaoFuncionalException badRequest(String message, List<Map<String, String>> violations) {
        return new AvaliacaoFuncionalException(HttpStatus.BAD_REQUEST, message, violations);
    }

    public static AvaliacaoFuncionalException unprocessable(String message, List<Map<String, String>> violations) {
        return new AvaliacaoFuncionalException(HttpStatus.UNPROCESSABLE_ENTITY, message, violations);
    }

    public static Map<String, String> violacao(String field, String message) {
        Map<String, String> v = new LinkedHashMap<>();
        v.put("field", field);
        v.put("message", message);
        return v;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<Map<String, String>> getViolations() {
        return violations;
    }
}
