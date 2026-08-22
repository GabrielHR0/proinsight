package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.document.AuditLogDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Trilha de auditoria (M5): registra quem fez o quê, em qual academia e quando.
 * A gravação nunca deve derrubar a requisição — falha é apenas logada.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(String actorUserId, String actorEmail, String academiaId,
                       String action, String targetType, String targetId,
                       int statusCode, Map<String, Object> details) {
        try {
            repository.save(new AuditLogDocument(
                    actorUserId, actorEmail, academiaId, action,
                    targetType, targetId, statusCode, details));
        } catch (RuntimeException e) {
            log.warn("Falha ao gravar auditoria (action={}): {}", action, e.getMessage());
        }
    }
}
