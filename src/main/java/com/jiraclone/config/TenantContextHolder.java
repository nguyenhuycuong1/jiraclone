package com.jiraclone.config;

public class TenantContextHolder {

    private static final ThreadLocal<String> tenantContext = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        tenantContext.set(tenantId);
    }

    public static String getTenantId() {
        return tenantContext.get();
    }

    public static boolean hasTenantId() {
        return tenantContext.get() != null;
    }

    public static void clear() {
        tenantContext.remove();
    }
}
