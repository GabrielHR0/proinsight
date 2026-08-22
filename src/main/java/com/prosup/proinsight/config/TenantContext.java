package com.prosup.proinsight.config;

public final class TenantContext {

    private static final ThreadLocal<String> ACADEMIA_ID = new InheritableThreadLocal<>();
    private static final ThreadLocal<Boolean> IN_HTTP_REQUEST = new InheritableThreadLocal<>();

    private TenantContext() {}

    public static void setAcademiaId(String academiaId) {
        ACADEMIA_ID.set(academiaId);
    }

    public static String getAcademiaId() {
        return ACADEMIA_ID.get();
    }

    public static void setInHttpRequest(boolean inHttpRequest) {
        IN_HTTP_REQUEST.set(inHttpRequest);
    }

    public static boolean isInHttpRequest() {
        return Boolean.TRUE.equals(IN_HTTP_REQUEST.get());
    }

    public static void clear() {
        ACADEMIA_ID.remove();
        IN_HTTP_REQUEST.remove();
    }
}
