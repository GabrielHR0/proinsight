package com.prosup.proinsight.infrastructure.persistence.document.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PersistedComposite implements PersistedComponent {

    private List<PersistedComponent> componentes = new ArrayList<>();

    public void addComponente(PersistedComponent componente) {
        if (componente == null) {
            throw new IllegalArgumentException("componente cannot be null");
        }
        componentes.add(componente);
    }

    public void removeComponente(PersistedComponent componente) {
        componentes.remove(componente);
    }

    public List<PersistedComponent> getComponentes() {
        return Collections.unmodifiableList(componentes);
    }

    public void setComponentes(List<PersistedComponent> componentes) {
        this.componentes = componentes == null ? new ArrayList<>() : new ArrayList<>(componentes);
    }
}
