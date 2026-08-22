package com.prosup.proinsight.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige um tenant (academia) ativo no TenantContext para executar o método.
 * Lança AccessDeniedException quando não há X-Academia-Id no contexto.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantCheck {
}