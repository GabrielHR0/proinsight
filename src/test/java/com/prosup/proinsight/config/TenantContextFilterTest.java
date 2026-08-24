package com.prosup.proinsight.config;

import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TenantContextFilterTest {

    private static final String ACADEMIA_A = "academia-a";
    private static final String ACADEMIA_B = "academia-b";

    private TenantContextFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TenantContextFilter();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private void autenticarComAcessoA(String... academiaIds) {
        var user = new User();
        user.setId("user-1");
        user.setEmail("user@test.com");
        user.setUserName("testuser");
        user.setAcademiaIds(Set.of(academiaIds));

        var permissoes = new java.util.HashMap<String, Set<Permissao>>();
        for (String id : academiaIds) {
            permissoes.put(id, Set.of(Permissao.CLIENTES_LER));
        }

        var authorities = List.of(new SimpleGrantedAuthority("CLIENTES_LER"));
        var principal = new CustomUserDetails(user, authorities, permissoes);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "", authorities));
    }

    @Test
    @DisplayName("deve definir tenant quando header corresponde a academia do usuario")
    void headerValidoDefineTenant() throws Exception {
        autenticarComAcessoA(ACADEMIA_A);
        var request = new MockHttpServletRequest();
        request.addHeader("X-Academia-Id", ACADEMIA_A);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        AtomicBoolean tenantVisible = new AtomicBoolean(false);
        AtomicBoolean inHttpVisible = new AtomicBoolean(false);

        org.mockito.Mockito.doAnswer(inv -> {
            tenantVisible.set(ACADEMIA_A.equals(TenantContext.getAcademiaId()));
            inHttpVisible.set(TenantContext.isInHttpRequest());
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        assertThat(tenantVisible).isTrue();
        assertThat(inHttpVisible).isTrue();
    }

    @Test
    @DisplayName("deve rejeitar com 403 header de academia sem acesso")
    void headerSemAcessoRejeita() throws Exception {
        autenticarComAcessoA(ACADEMIA_A);
        var request = new MockHttpServletRequest();
        request.addHeader("X-Academia-Id", ACADEMIA_B);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        org.mockito.Mockito.doAnswer(inv -> {
            chainCalled.set(true);
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(chainCalled).isFalse();
        assertThat(TenantContext.getAcademiaId()).isNull();
    }

    @Test
    @DisplayName("deve retornar 403 quando header X-Academia-Id é enviado sem autenticacao")
    void headerSemAutenticacaoDeveRetornar403() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Academia-Id", ACADEMIA_A);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(TenantContext.getAcademiaId()).isNull();
    }

    @Test
    @DisplayName("deve limpar o tenant ao final da requisicao")
    void limpaTenantAposRequisicao() throws Exception {
        autenticarComAcessoA(ACADEMIA_A);
        var request = new MockHttpServletRequest();
        request.addHeader("X-Academia-Id", ACADEMIA_A);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(TenantContext.getAcademiaId()).isNull();
        assertThat(TenantContext.isInHttpRequest()).isFalse();
    }

    @Test
    @DisplayName("deve usar userId como tenant quando usuario nao tem academias e nao envia header")
    void usuarioSemAcademiaUsaUserIdComoTenant() throws Exception {
        var user = new User();
        user.setId("personal-user-id");
        user.setEmail("personal@test.com");
        user.setUserName("personal");
        user.setAcademiaIds(Set.of());

        var permissoes = new java.util.HashMap<String, Set<Permissao>>();
        // Mapa vazio = sem academia real

        var authorities = List.of(new SimpleGrantedAuthority("CLIENTES_LER"));
        var principal = new CustomUserDetails(user, authorities, permissoes);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "", authorities));

        var request = new MockHttpServletRequest();
        // Sem header X-Academia-Id
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        AtomicBoolean tenantVisible = new AtomicBoolean(false);

        org.mockito.Mockito.doAnswer(inv -> {
            tenantVisible.set("personal-user-id".equals(TenantContext.getAcademiaId()));
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        assertThat(tenantVisible).isTrue();
    }

    @Test
    @DisplayName("deve manter tenant null quando usuario COM academias nao envia header")
    void usuarioComAcademiaSemHeaderNaoDefineTenant() throws Exception {
        autenticarComAcessoA(ACADEMIA_A);
        var request = new MockHttpServletRequest();
        // Sem header X-Academia-Id
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(TenantContext.getAcademiaId()).isNull();
    }
}
