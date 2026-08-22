package com.prosup.proinsight.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AcademiaScopingEventListener extends AbstractMongoEventListener<Object> {

    private static final Logger log = LoggerFactory.getLogger(AcademiaScopingEventListener.class);

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        var entity = event.getSource();
        if (entity == null || !entity.getClass().isAnnotationPresent(ScopedByAcademia.class)) {
            return;
        }

        String tenantId = TenantContext.getAcademiaId();
        if (tenantId == null || tenantId.isBlank()) {
            if (TenantContext.isInHttpRequest()) {
                throw new AccessDeniedException(
                        "Salvar dados de academia exige X-Academia-Id");
            }
            return;
        }

        try {
            var getter = entity.getClass().getMethod("getAcademiaId");
            Object current = getter.invoke(entity);
            if (current == null || (current instanceof String s && s.isBlank())) {
                var setter = entity.getClass().getMethod("setAcademiaId", String.class);
                setter.invoke(entity, tenantId);
            } else if (current instanceof String s && !s.equals(tenantId)) {
                throw new AccessDeniedException(
                        "academiaId do documento não corresponde ao X-Academia-Id");
            }
        } catch (NoSuchMethodException e) {
            // entidade não tem campo academiaId — ignorar
        } catch (Exception e) {
            if (e instanceof AccessDeniedException ade) {
                throw ade;
            }
            log.warn("Erro ao setar academiaId automaticamente em {}", entity.getClass().getSimpleName(), e);
        }
    }
}
