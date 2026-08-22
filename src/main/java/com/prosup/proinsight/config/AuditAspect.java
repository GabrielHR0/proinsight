package com.prosup.proinsight.config;

import com.prosup.proinsight.api.annotation.Audited;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Trilha de auditoria (M5): intercepta endpoints anotados com @Audited
 * e registra quem, quando, em qual academia e qual recurso foi alterado.
 * A gravação é best-effort: falhas de persistência não derrubam o fluxo.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @AfterReturning(pointcut = "@annotation(audited)", returning = "result")
    public void audit(JoinPoint joinPoint, Audited audited, Object result) {
        try {
            var request = currentRequest();
            String uri = request != null ? request.getRequestURI() : joinPoint.getSignature().toShortString();
            String httpMethod = request != null ? request.getMethod() : "";

            String actorId = null;
            String actorEmail = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                actorId = userDetails.getUser().getId();
                actorEmail = userDetails.getUser().getEmail();
            }

            int status = extractStatus(result);
            String targetType = extractTargetType(result);
            String targetId = extractTargetId(result);

            Map<String, Object> details = new HashMap<>();
            details.put("httpMethod", httpMethod);
            details.put("uri", uri);

            auditLogService.record(
                    actorId,
                    actorEmail,
                    TenantContext.getAcademiaId(),
                    joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName(),
                    targetType,
                    targetId,
                    status,
                    details);
        } catch (Exception e) {
            log.warn("Falha ao auditar {}: {}", joinPoint.getSignature(), e.getMessage());
        }
    }

    private static HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }

    private static int extractStatus(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }
        return 200;
    }

    private static String extractTargetType(Object result) {
        Object body = unwrapBody(result);
        return body != null ? body.getClass().getSimpleName() : null;
    }

    private static String extractTargetId(Object result) {
        Object body = unwrapBody(result);
        if (body == null) return null;

        Optional<Object> idValue = extractId(body);
        return idValue.map(String::valueOf).orElse(null);
    }

    private static Object unwrapBody(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            if (body instanceof Map<?, ?> map && map.containsKey("id")) {
                return map;
            }
            return body;
        }
        return result;
    }

    private static Optional<Object> extractId(Object body) {
        if (body instanceof Map<?, ?> map) {
            return Optional.ofNullable(map.get("id"));
        }
        try {
            var idMethod = body.getClass().getMethod("getId");
            return Optional.ofNullable(idMethod.invoke(body));
        } catch (Exception ignored) {
            // sem getId — sem id para auditar
        }
        return Optional.empty();
    }
}
