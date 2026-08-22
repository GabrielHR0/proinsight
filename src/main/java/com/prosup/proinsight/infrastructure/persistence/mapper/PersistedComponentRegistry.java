package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.classes.NivelForca;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaEquipamento;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaIdade;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaVo2Max;
import com.prosup.proinsight.infrastructure.persistence.document.composite.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class PersistedComponentRegistry {

    private final Map<Class<?>, String> classToKey = new HashMap<>();
    private final Map<String, Function<PersistedComponent, Component>> toDomainFns = new HashMap<>();
    private final Map<String, Function<Component, PersistedComponent>> toPersistedFns = new HashMap<>();

    public Component toDomain(PersistedComponent persisted) {
        if (persisted == null) return null;
        var key = classToKey.get(persisted.getClass());
        if (key == null)
            throw new IllegalArgumentException("PersistedComponent não registrado: " + persisted.getClass());
        return toDomainFns.get(key).apply(persisted);
    }

    public PersistedComponent toPersisted(Component domain) {
        if (domain == null) return null;
        var key = classToKey.get(domain.getClass());
        if (key == null)
            throw new IllegalArgumentException("Component não registrado: " + domain.getClass());
        return toPersistedFns.get(key).apply(domain);
    }

    @SuppressWarnings("unchecked")
    private void register(
            String key,
            Class<? extends PersistedComponent> persistedClass,
            Class<? extends Component> domainClass,
            Function<? super PersistedComponent, ? extends Component> toDomainFn,
            Function<? super Component, ? extends PersistedComponent> toPersistedFn
    ) {
        classToKey.put(persistedClass, key);
        classToKey.put(domainClass, key);
        toDomainFns.put(key, (Function<PersistedComponent, Component>) toDomainFn);
        toPersistedFns.put(key, (Function<Component, PersistedComponent>) toPersistedFn);
    }

    @PostConstruct
    private void init() {
        register("persistedNivelForca", PersistedNivelForca.class, NivelForca.class,
                this::toDomainNivelForca, this::toPersistedNivelForca);
        register("persistedNivelVo2Max", PersistedNivelVo2Max.class, NivelVo2Max.class,
                this::toDomainNivelVo2Max, this::toPersistedNivelVo2Max);
        register("persistedNivelImc", PersistedNivelImc.class, NivelImc.class,
                this::toDomainNivelImc, this::toPersistedNivelImc);
        register("persistedTabelaVo2Max", PersistedTabelaVo2Max.class, TabelaVo2Max.class,
                this::toDomainTabelaVo2Max, this::toPersistedTabelaVo2Max);
        register("persistedTabelaSexo", PersistedTabelaSexo.class, TabelaSexo.class,
                this::toDomainTabelaSexo, this::toPersistedTabelaSexo);
        register("persistedTabelaEquipamento", PersistedTabelaEquipamento.class, TabelaEquipamento.class,
                this::toDomainTabelaEquipamento, this::toPersistedTabelaEquipamento);
        register("persistedTabelaIdade", PersistedTabelaIdade.class, TabelaIdade.class,
                this::toDomainTabelaIdade, this::toPersistedTabelaIdade);
        register("persistedTabelaClassificacaoGenerica", PersistedTabelaClassificacaoGenerica.class, TabelaClassificacaoGenerica.class,
                this::toDomainTabelaClassificacaoGenerica, this::toPersistedTabelaClassificacaoGenerica);
    }

    // ========== Leaf converters ==========

    private Component toDomainNivelForca(PersistedComponent p) {
        var src = (PersistedNivelForca) p;
        var dst = new NivelForca(src.getClassificacao(), src.getMin(), src.getMax());
        if (src.getTipoMin() != null) dst.setTipoMin(src.getTipoMin());
        if (src.getTipoMax() != null) dst.setTipoMax(src.getTipoMax());
        dst.setNome(src.getNome());
        dst.setNivel(src.getNivel());
        return dst;
    }

    private PersistedComponent toPersistedNivelForca(Component d) {
        var src = (NivelForca) d;
        var dst = new PersistedNivelForca(src.getClassificacao(), src.getMin(), src.getMax(),
                src.getTipoMin(), src.getTipoMax());
        dst.setNome(src.getNome());
        dst.setNivel(src.getNivel());
        return dst;
    }

    private Component toDomainNivelVo2Max(PersistedComponent p) {
        var src = (PersistedNivelVo2Max) p;
        var dst = new NivelVo2Max(src.getClassificacao(), src.getMin(), src.getMax());
        if (src.getTipoMin() != null) dst.setTipoMin(src.getTipoMin());
        if (src.getTipoMax() != null) dst.setTipoMax(src.getTipoMax());
        return dst;
    }

    private PersistedComponent toPersistedNivelVo2Max(Component d) {
        var src = (NivelVo2Max) d;
        return new PersistedNivelVo2Max(src.getClassificacao(), src.getMin(), src.getMax(),
                src.getTipoMin(), src.getTipoMax());
    }

    private Component toDomainNivelImc(PersistedComponent p) {
        var src = (PersistedNivelImc) p;
        var dst = new NivelImc(src.getClassificacao(), src.getMin(), src.getMax());
        if (src.getTipoMin() != null) dst.setTipoMin(src.getTipoMin());
        if (src.getTipoMax() != null) dst.setTipoMax(src.getTipoMax());
        return dst;
    }

    private PersistedComponent toPersistedNivelImc(Component d) {
        var src = (NivelImc) d;
        return new PersistedNivelImc(src.getClassificacao(), src.getMin(), src.getMax(),
                src.getTipoMin(), src.getTipoMax());
    }

    // ========== Composite converters ==========

    private Component toDomainTabelaVo2Max(PersistedComponent p) {
        var src = (PersistedTabelaVo2Max) p;
        var dst = new TabelaVo2Max();
        dst.setProtocolo(src.getProtocolo());
        for (var child : src.getComponentes()) {
            dst.add(toDomain(child));
        }
        return dst;
    }

    private PersistedComponent toPersistedTabelaVo2Max(Component d) {
        var src = (TabelaVo2Max) d;
        var dst = new PersistedTabelaVo2Max();
        dst.setProtocolo(src.getProtocolo());
        for (var child : src.getChildren()) {
            dst.addComponente(toPersisted(child));
        }
        return dst;
    }

    private Component toDomainTabelaSexo(PersistedComponent p) {
        var src = (PersistedTabelaSexo) p;
        var dst = new TabelaSexo();
        dst.setSexo(src.getSexo());
        for (var child : src.getComponentes()) {
            dst.add(toDomain(child));
        }
        return dst;
    }

    private PersistedComponent toPersistedTabelaSexo(Component d) {
        var src = (TabelaSexo) d;
        var dst = new PersistedTabelaSexo();
        dst.setSexo(src.getSexo());
        for (var child : src.getChildren()) {
            dst.addComponente(toPersisted(child));
        }
        return dst;
    }

    private Component toDomainTabelaEquipamento(PersistedComponent p) {
        var src = (PersistedTabelaEquipamento) p;
        var dst = new TabelaEquipamento();
        dst.setEquipamento(src.getEquipamento());
        for (var child : src.getComponentes()) {
            dst.add(toDomain(child));
        }
        return dst;
    }

    private PersistedComponent toPersistedTabelaEquipamento(Component d) {
        var src = (TabelaEquipamento) d;
        var dst = new PersistedTabelaEquipamento();
        dst.setEquipamento(src.getEquipamento());
        for (var child : src.getChildren()) {
            dst.addComponente(toPersisted(child));
        }
        return dst;
    }

    private Component toDomainTabelaIdade(PersistedComponent p) {
        var src = (PersistedTabelaIdade) p;
        var dst = new TabelaIdade();
        dst.setIdadeMin(src.getIdadeMin());
        dst.setIdadeMax(src.getIdadeMax());
        for (var child : src.getComponentes()) {
            dst.add(toDomain(child));
        }
        return dst;
    }

    private PersistedComponent toPersistedTabelaIdade(Component d) {
        var src = (TabelaIdade) d;
        var dst = new PersistedTabelaIdade();
        dst.setIdadeMin(src.getIdadeMin());
        dst.setIdadeMax(src.getIdadeMax());
        for (var child : src.getChildren()) {
            dst.addComponente(toPersisted(child));
        }
        return dst;
    }

    private Component toDomainTabelaClassificacaoGenerica(PersistedComponent p) {
        var src = (PersistedTabelaClassificacaoGenerica) p;
        var dst = new TabelaClassificacaoGenerica();
        for (var child : src.getComponentes()) {
            dst.add(toDomain(child));
        }
        return dst;
    }

    private PersistedComponent toPersistedTabelaClassificacaoGenerica(Component d) {
        var src = (TabelaClassificacaoGenerica) d;
        var dst = new PersistedTabelaClassificacaoGenerica();
        for (var child : src.getChildren()) {
            dst.addComponente(toPersisted(child));
        }
        return dst;
    }
}
