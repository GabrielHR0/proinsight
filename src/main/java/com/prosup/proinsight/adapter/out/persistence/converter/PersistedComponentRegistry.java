package com.prosup.proinsight.adapter.out.persistence.converter;

import com.prosup.proinsight.adapter.out.persistence.composite.*;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelForca;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaEquipamento;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaVo2Max;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


@Service
public class PersistedComponentRegistry {
    
    private final Map<String, Supplier<? extends Component>> domainSuppliers = new HashMap<>();
    private final Map<String, Supplier<? extends PersistedComponent>> persistedSuppliers = new HashMap<>();
    
    public PersistedComponentRegistry() {
        registrarDomain("persistedNivelForca", NivelForca::new);
        registrarDomain("persistedTabelaVo2Max", TabelaVo2Max::new);
        registrarDomain("persistedTabelaSexo", TabelaSexo::new);
        registrarDomain("persistedTabelaEquipamento", TabelaEquipamento::new);
        
        registrarPersisted("nivelForca", PersistedNivelForca::new);
        registrarPersisted("tabelaVo2Max", PersistedTabelaVo2Max::new);
        registrarPersisted("tabelaSexo", PersistedTabelaSexo::new);
        registrarPersisted("tabelaEquipamento", PersistedTabelaEquipamento::new);
    }
    
    public void registrarDomain(String typeAlias, Supplier<? extends Component> supplier) {
        domainSuppliers.put(typeAlias, supplier);
    }
    
    public void registrarPersisted(String typeAlias, Supplier<? extends PersistedComponent> supplier) {
        persistedSuppliers.put(typeAlias, supplier);
    }
    
    public Supplier<? extends Component> getDomainSupplier(String typeAlias) {
        return domainSuppliers.get(typeAlias);
    }
    
    public Supplier<? extends PersistedComponent> getPersistedSupplier(String typeAlias) {
        return persistedSuppliers.get(typeAlias);
    }
    
    public String getTipoParaPersisted(PersistedComponent persisted) {
        if (persisted instanceof PersistedNivelForca) return "nivelForca";
        if (persisted instanceof PersistedTabelaVo2Max) return "tabelaVo2Max";
        if (persisted instanceof PersistedTabelaSexo) return "tabelaSexo";
        if (persisted instanceof PersistedTabelaEquipamento) return "tabelaEquipamento";
        throw new IllegalArgumentException("Tipo Persisted desconhecido: " + persisted.getClass());
    }
    
    public String getTipoParaDomain(Component component) {
        if (component instanceof NivelForca) return "nivelForca";
        if (component instanceof TabelaVo2Max) return "tabelaVo2Max";
        if (component instanceof TabelaSexo) return "tabelaSexo";
        if (component instanceof TabelaEquipamento) return "tabelaEquipamento";
        throw new IllegalArgumentException("Tipo Domain desconhecido: " + component.getClass());
    }
}
