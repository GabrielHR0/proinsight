package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponent;
import org.springframework.stereotype.Service;

@Service
public class PersistedComponentMapper {

    private final PersistedComponentRegistry registry;

    public PersistedComponentMapper(PersistedComponentRegistry registry) {
        this.registry = registry;
    }

    public Component toDomain(PersistedComponent persisted) {
        return registry.toDomain(persisted);
    }

    public PersistedComponent toPersisted(Component domain) {
        return registry.toPersisted(domain);
    }
}
