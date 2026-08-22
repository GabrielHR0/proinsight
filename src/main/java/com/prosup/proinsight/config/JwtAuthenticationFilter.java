package com.prosup.proinsight.config;

import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.service.UserPermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserPermissionService userPermissionService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserPermissionService userPermissionService) {
        this.tokenProvider = tokenProvider;
        this.userPermissionService = userPermissionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            if (tokenProvider.validateToken(token)) {
                try {
                    Authentication auth = tokenProvider.getAuthentication(token, request);
                    SecurityContextHolder.getContext().setAuthentication(
                            refreshAuthorities(auth, request.getHeader("X-Academia-Id")));
                } catch (RuntimeException e) {
                    log.warn("Falha ao autenticar token JWT: IP={}, URI={}, motivo={}",
                            request.getRemoteAddr(), request.getRequestURI(), e.getMessage());
                }
            } else {
                log.warn("Tentativa de acesso com token JWT inválido: IP={}, URI={}",
                        request.getRemoteAddr(), request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Re-resolve as permissões do usuário a partir do banco (com cache de 60s)
     * em vez de confiar nas claims do JWT, que ficam congeladas por 24h.
     * Usuário deletado/desativado deixa de autenticar imediatamente (fail-closed).
     */
    private Authentication refreshAuthorities(Authentication auth, String academiaId) {
        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return auth;
        }
        User user = userDetails.getUser();
        Map<String, Set<Permissao>> freshPermissoes =
                userPermissionService.loadAcademiaPermissoes(user.getId());

        log.debug("[AUTH] userId={}, academiaId.header={}, academias.do.usuario={}, permissoes.map={}",
                user.getId(),
                academiaId,
                freshPermissoes.keySet(),
                freshPermissoes.keySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                k -> k,
                                k -> freshPermissoes.get(k).stream().map(Enum::name).toList())));

        Set<Permissao> effective;
        if (academiaId == null) {
            log.warn("[AUTH] Header X-Academia-Id ausente. userId={}, academias.disponiveis={}",
                    user.getId(), freshPermissoes.keySet());
            effective = Set.of();
        } else if (!freshPermissoes.containsKey(academiaId)) {
            log.warn("[AUTH] X-Academia-Id={} não encontrado nas academias do usuário. userId={}, academias.disponiveis={}",
                    academiaId, user.getId(), freshPermissoes.keySet());
            effective = Set.of();
        } else {
            effective = freshPermissoes.get(academiaId);
            log.debug("[AUTH] Permissões resolvidas: userId={}, academiaId={}, permissoes={}",
                    user.getId(), academiaId, effective.stream().map(Enum::name).toList());
        }

        var authorities = effective.stream()
                .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p.name()))
                .toList();

        CustomUserDetails refreshed = new CustomUserDetails(user, authorities, freshPermissoes);
        return new UsernamePasswordAuthenticationToken(refreshed, "", authorities);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
