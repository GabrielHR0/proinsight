package com.prosup.proinsight.config;

import org.springframework.core.task.TaskDecorator;

public class TenantContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        String academiaId = TenantContext.getAcademiaId();
        return () -> {
            try {
                if (academiaId != null) {
                    TenantContext.setAcademiaId(academiaId);
                }
                runnable.run();
            } finally {
                TenantContext.clear();
            }
        };
    }
}
