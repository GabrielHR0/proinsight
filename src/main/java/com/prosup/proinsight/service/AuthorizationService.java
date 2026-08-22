package com.prosup.proinsight.service;

import com.prosup.proinsight.config.TenantContext;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("auth")
public class AuthorizationService {

    public boolean hasAcademiaAccess(String academiaId) {
        String effectiveId = academiaId != null ? academiaId : TenantContext.getAcademiaId();
        if (effectiveId == null) return false;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getAcademiaPermissoes() != null
                    && userDetails.getAcademiaPermissoes().containsKey(effectiveId);
        }
        return false;
    }

    public boolean hasAnyAcademiaAccess(Collection<String> academiaIds) {
        if (academiaIds == null || academiaIds.isEmpty()) return false;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getAcademiaPermissoes() != null
                    && userDetails.getAcademiaPermissoes().keySet().containsAll(academiaIds);
        }
        return false;
    }

    public boolean isCurrentUser(String userId) {
        if (userId == null) return false;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userId.equals(userDetails.getUser().getId());
        }
        return false;
    }

    public boolean isSuperAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN"));
    }
}
