package com.prosup.proinsight.api.dto.request;

import com.prosup.proinsight.domain.enums.Permissao;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public class RoleRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    private Set<Permissao> permissoes;

    public RoleRequest() {
    }

    public RoleRequest(String name, String description, Set<Permissao> permissoes) {
        this.name = name;
        this.description = description;
        this.permissoes = permissoes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(Set<Permissao> permissoes) {
        this.permissoes = permissoes;
    }
}
