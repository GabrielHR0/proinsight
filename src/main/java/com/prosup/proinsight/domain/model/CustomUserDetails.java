package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.Permissao;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Set<Permissao>> academiaPermissoes;

    public CustomUserDetails(User user,
                             Collection<? extends GrantedAuthority> authorities,
                             Map<String, Set<Permissao>> academiaPermissoes)
    {
        this.user = user;
        this.authorities = authorities;
        this.academiaPermissoes = academiaPermissoes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public User getUser() {
        return user;
    }

    public Map<String, Set<Permissao>> getAcademiaPermissoes() {
        return academiaPermissoes;
    }
}
