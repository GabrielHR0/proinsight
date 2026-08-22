package com.prosup.proinsight.config;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Garante que métodos anotados com @TenantCheck só executem com
 * um tenant (academia) ativo no TenantContext.
 */
@Aspect
@Component
public class TenantCheckAspect {

    @Before("@annotation(com.prosup.proinsight.config.TenantCheck)")
    public void requireTenant() {
        String tenantId = TenantContext.getAcademiaId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new AccessDeniedException(
                "Acesso a dados de academia exige X-Academia-Id");
        }
    }
}