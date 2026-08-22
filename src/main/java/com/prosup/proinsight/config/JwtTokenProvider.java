package com.prosup.proinsight.config;

import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final Long expiration;
    private final String issuer;
    private final String audience;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") Long expiration,
        @Value("${jwt.issuer}") String issuer,
        @Value("${jwt.audience}") String audience,
        TokenBlacklistService tokenBlacklistService
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
        this.issuer = issuer;
        this.audience = audience;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, List<String>> academiaPermissoesClaims = new HashMap<>();
        if (userDetails.getAcademiaPermissoes() != null) {
            for (var entry : userDetails.getAcademiaPermissoes().entrySet()) {
                academiaPermissoesClaims.put(
                    entry.getKey(),
                    entry.getValue().stream().map(Enum::name).toList()
                );
            }
        }

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("userName", user.getUserName())
                .claim("academiaIds", user.getAcademiaIds() != null
                    ? List.copyOf(user.getAcademiaIds())
                    : List.of()
                )
                .claim("academiaPermissoes", academiaPermissoesClaims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token, HttpServletRequest request) {
        Claims claims = createParser().build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getId() == null || claims.getId().isBlank()) {
            throw new JwtException("Token JWT sem jti");
        }

        if (tokenBlacklistService.isRevoked(claims.getId())) {
            throw new JwtException("Token JWT revogado");
        }

        String email = claims.getSubject();
        String userId = claims.get("userId", String.class);
        String userName = claims.get("userName", String.class);

        @SuppressWarnings("unchecked")
        List<String> academiaIdsList = claims.get("academiaIds", List.class);

        @SuppressWarnings("unchecked")
        Map<String, List<String>> academiaPermissoes = claims.get("academiaPermissoes", Map.class);

        String academiaId = request.getHeader("X-Academia-Id");

        List<String> effectivePermissions;
        if (academiaId != null && academiaPermissoes != null
                && academiaPermissoes.containsKey(academiaId)) {
            effectivePermissions = academiaPermissoes.get(academiaId);
        } else {
            effectivePermissions = List.of();
        }

        var authorities = effectivePermissions.stream()
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setUserName(userName);
        if (academiaIdsList != null) {
            user.setAcademiaIds(Set.copyOf(academiaIdsList));
        }

        Map<String, Set<Permissao>> permissoes = new HashMap<>();
        if (academiaPermissoes != null) {
            for (var entry : academiaPermissoes.entrySet()) {
                Set<Permissao> perms = entry.getValue().stream()
                        .map(Permissao::valueOf)
                        .collect(Collectors.toSet());
                permissoes.put(entry.getKey(), perms);
            }
        }

        CustomUserDetails principal = new CustomUserDetails(user, authorities, permissoes);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public long getExpirationMs() {
        return expiration;
    }

    public boolean validateToken(String token) {
        try {
            var claims = createParser().build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getId() != null && !claims.getId().isBlank()
                    && !tokenBlacklistService.isRevoked(claims.getId());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getJti(String token) {
        return createParser().build()
                .parseSignedClaims(token)
                .getPayload()
                .getId();
    }

    private JwtParserBuilder createParser() {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .requireAudience(audience);
    }

}
