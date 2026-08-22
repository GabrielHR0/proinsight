package com.prosup.proinsight.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca endpoints que devem ser registrados na trilha de auditoria.
 * O aspecto captura autor, academia, ação, alvo e status da resposta.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited {
}
