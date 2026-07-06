package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.infrastructure.persistence.document.composite.*;
import com.prosup.proinsight.domain.enums.Equipamento;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.composite.*;
import com.prosup.proinsight.domain.model.composite.classes.NivelForca;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaEquipamento;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaIdade;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaVo2Max;
import org.springframework.stereotype.Service;


@Service
public class PersistedComponentMapper {
    
    private final PersistedComponentRegistry registry;
    
    public PersistedComponentMapper(PersistedComponentRegistry registry) {
        this.registry = registry;
    }

    public Component toDomain(PersistedComponent persisted) {
        if (persisted == null) {
            return null;
        }
        
        if (persisted instanceof PersistedLeaf) {
            return toLeafDomain((PersistedLeaf) persisted);
        }
        
        if (persisted instanceof PersistedComposite) {
            return toCompositeDomain((PersistedComposite) persisted);
        }
        
        throw new IllegalArgumentException("Tipo Persisted desconhecido: " + persisted.getClass());
    }
    
    private Leaf toLeafDomain(PersistedLeaf persisted) {
        if (persisted instanceof PersistedNivelForca) {
            PersistedNivelForca p = (PersistedNivelForca) persisted;
            NivelForca domain = new NivelForca(p.getClassificacao(), p.getMin(), p.getMax());
            if (p.getTipoMin() != null) domain.setTipoMin(p.getTipoMin());
            if (p.getTipoMax() != null) domain.setTipoMax(p.getTipoMax());
            return domain;
        }
        if (persisted instanceof PersistedNivelVo2Max) {
            PersistedNivelVo2Max p = (PersistedNivelVo2Max) persisted;
            NivelVo2Max domain = new NivelVo2Max(p.getClassificacao(), p.getMin(), p.getMax());
            if (p.getTipoMin() != null) domain.setTipoMin(p.getTipoMin());
            if (p.getTipoMax() != null) domain.setTipoMax(p.getTipoMax());
            return domain;
        }
        throw new IllegalArgumentException("Tipo PersistedLeaf desconhecido: " + persisted.getClass());
    }
    
    private Composite toCompositeDomain(PersistedComposite persisted) {
        Composite domain;
        
        if (persisted instanceof PersistedTabelaVo2Max) {
            PersistedTabelaVo2Max p = (PersistedTabelaVo2Max) persisted;
            TabelaVo2Max result = new TabelaVo2Max();
            result.setProtocolo(p.getProtocolo());
            converterFilhos(p, result);
            domain = result;
        } else if (persisted instanceof PersistedTabelaSexo) {
            PersistedTabelaSexo p = (PersistedTabelaSexo) persisted;
            TabelaSexo result = new TabelaSexo();
            result.setSexo(p.getSexo());
            converterFilhos(p, result);
            domain = result;
        } else if (persisted instanceof PersistedTabelaEquipamento) {
            PersistedTabelaEquipamento p = (PersistedTabelaEquipamento) persisted;
            TabelaEquipamento result = new TabelaEquipamento();
            result.setEquipamento(p.getEquipamento());
            converterFilhos(p, result);
            domain = result;
        } else if (persisted instanceof PersistedTabelaIdade) {
            PersistedTabelaIdade p = (PersistedTabelaIdade) persisted;
            TabelaIdade result = new TabelaIdade();
            result.setIdadeMin(p.getIdadeMin());
            result.setIdadeMax(p.getIdadeMax());
            converterFilhos(p, result);
            domain = result;
        } else {
            throw new IllegalArgumentException("Tipo PersistedComposite desconhecido: " + persisted.getClass());
        }
        
        return domain;
    }
    
    private void converterFilhos(PersistedComposite persistedParent, Composite domainParent) {
        for (PersistedComponent persistedChild : persistedParent.getComponentes()) {
            Component domainChild = toDomain(persistedChild);
            if (domainChild != null) {
                domainParent.add(domainChild);
            }
        }
    }
    
    // ============ CONVERSÃO DOMAIN → PERSISTED ============
    
    /**
     * Converte Component para PersistedComponent.
     * Funciona polimorficamente para qualquer tipo Domain.
     */
    public PersistedComponent toPersisted(Component domain) {
        if (domain == null) {
            return null;
        }
        
        if (domain instanceof Leaf) {
            return toPersistedLeaf((Leaf) domain);
        }
        
        if (domain instanceof Composite) {
            return toPersistedComposite((Composite) domain);
        }
        
        throw new IllegalArgumentException("Tipo Domain desconhecido: " + domain.getClass());
    }
    
    private PersistedLeaf toPersistedLeaf(Leaf domain) {
        if (domain instanceof NivelForca) {
            NivelForca n = (NivelForca) domain;
            return new PersistedNivelForca(n.getClassificacao(), n.getMin(), n.getMax(), n.getTipoMin(), n.getTipoMax());
        }
        if (domain instanceof NivelVo2Max) {
            NivelVo2Max n = (NivelVo2Max) domain;
            return new PersistedNivelVo2Max(n.getClassificacao(), n.getMin(), n.getMax(), n.getTipoMin(), n.getTipoMax());
        }
        throw new IllegalArgumentException("Tipo Leaf desconhecido: " + domain.getClass());
    }
    
    private PersistedComposite toPersistedComposite(Composite domain) {
        PersistedComposite persisted;
        
        if (domain instanceof TabelaVo2Max) {
            TabelaVo2Max t = (TabelaVo2Max) domain;
            PersistedTabelaVo2Max result = new PersistedTabelaVo2Max();
            result.setProtocolo(t.getProtocolo());
            converterFilhosPersisted(t, result);
            persisted = result;
        } else if (domain instanceof TabelaSexo) {
            TabelaSexo t = (TabelaSexo) domain;
            PersistedTabelaSexo result = new PersistedTabelaSexo();
            result.setSexo(t.getSexo());
            converterFilhosPersisted(t, result);
            persisted = result;
        } else if (domain instanceof TabelaEquipamento) {
            TabelaEquipamento t = (TabelaEquipamento) domain;
            PersistedTabelaEquipamento result = new PersistedTabelaEquipamento();
            result.setEquipamento(t.getEquipamento());
            converterFilhosPersisted(t, result);
            persisted = result;
        } else if (domain instanceof TabelaIdade) {
            TabelaIdade t = (TabelaIdade) domain;
            PersistedTabelaIdade result = new PersistedTabelaIdade();
            result.setIdadeMin(t.getIdadeMin());
            result.setIdadeMax(t.getIdadeMax());
            converterFilhosPersisted(t, result);
            persisted = result;
        } else {
            throw new IllegalArgumentException("Tipo Composite desconhecido: " + domain.getClass());
        }
        
        return persisted;
    }
    
    private void converterFilhosPersisted(Composite domainParent, PersistedComposite persistedParent) {
        for (Component domainChild : domainParent.getChildren()) {
            PersistedComponent persistedChild = toPersisted(domainChild);
            if (persistedChild != null) {
                persistedParent.addComponente(persistedChild);
            }
        }
    }
}
