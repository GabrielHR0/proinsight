package com.prosup.proinsight.config;

import com.prosup.proinsight.domain.model.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            TenantContext.setInHttpRequest(true);
            String academiaId = request.getHeader("X-Academia-Id");
            if (academiaId != null && !academiaId.isBlank()) {
                if (!hasAccess(academiaId)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"message\":\"Acesso à academia " + academiaId + " não permitido\"}");
                    return;
                }
                TenantContext.setAcademiaId(academiaId);
            } else {
                // Sem header X-Academia-Id: usa userId como tenant pessoal (para personal autônomo)
                String personalTenant = resolvePersonalTenant();
                if (personalTenant != null) {
                    TenantContext.setAcademiaId(personalTenant);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolvePersonalTenant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            var permissoes = userDetails.getAcademiaPermissoes();
            if (permissoes != null && !permissoes.isEmpty()) {
                // Tem academia(s) real(is) → precisa enviar X-Academia-Id
                return null;
            }
            // Sem academia: userId é o tenant pessoal
            return userDetails.getUser().getId();
        }
        return null;
    }

    private boolean hasAccess(String academiaId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }

        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getAcademiaPermissoes() != null
                    && userDetails.getAcademiaPermissoes().containsKey(academiaId);
        }

        // Autenticação por API key: o tenant é definido pela própria chave.
        String keyTenant = TenantContext.getAcademiaId();
        return keyTenant == null || keyTenant.equals(academiaId);
    }
}
