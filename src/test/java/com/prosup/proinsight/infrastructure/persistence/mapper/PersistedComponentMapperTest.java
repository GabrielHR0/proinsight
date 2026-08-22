package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.classes.NivelForca;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaEquipamento;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaIdade;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaVo2Max;
import com.prosup.proinsight.infrastructure.persistence.document.composite.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class PersistedComponentMapperTest {

    private PersistedComponentMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        var registry = new PersistedComponentRegistry();
        Method init = PersistedComponentRegistry.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(registry);
        mapper = new PersistedComponentMapper(registry);
    }

    @Test
    void shouldRoundTripNivelForca() {
        var original = new NivelForca("Bom", 10.0, 20.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO);

        PersistedComponent persisted = mapper.toPersisted(original);
        Component result = mapper.toDomain(persisted);

        assertThat(result).isInstanceOf(NivelForca.class);
        assertThat(((NivelForca) result).getClassificacao()).isEqualTo("Bom");
        assertThat(((NivelForca) result).getMin()).isEqualTo(10.0);
        assertThat(((NivelForca) result).getMax()).isEqualTo(20.0);
        assertThat(((NivelForca) result).getTipoMin()).isEqualTo(TipoLimite.INCLUSIVO);
        assertThat(((NivelForca) result).getTipoMax()).isEqualTo(TipoLimite.EXCLUSIVO);
    }

    @Test
    void shouldRoundTripNivelVo2Max() {
        var original = new NivelVo2Max("Excelente", 50.0, 60.0);

        PersistedComponent persisted = mapper.toPersisted(original);
        Component result = mapper.toDomain(persisted);

        assertThat(result).isInstanceOf(NivelVo2Max.class);
        assertThat(((NivelVo2Max) result).getClassificacao()).isEqualTo("Excelente");
        assertThat(((NivelVo2Max) result).getMin()).isEqualTo(50.0);
        assertThat(((NivelVo2Max) result).getMax()).isEqualTo(60.0);
    }

    @Test
    void shouldRoundTripTabelaVo2MaxWithChildren() {
        var original = new TabelaVo2Max();
        original.setProtocolo(Protocolo.COOPER);
        original.add(new NivelVo2Max("Ruim", 0.0, 25.0));
        original.add(new NivelVo2Max("Bom", 25.0, 40.0));
        original.add(new NivelVo2Max("Excelente", 40.0, 100.0));

        PersistedComponent persisted = mapper.toPersisted(original);
        Component result = mapper.toDomain(persisted);

        assertThat(result).isInstanceOf(TabelaVo2Max.class);
        var tabela = (TabelaVo2Max) result;
        assertThat(tabela.getProtocolo()).isEqualTo(Protocolo.COOPER);
        assertThat(tabela.getChildren()).hasSize(3);
        assertThat(tabela.getChildren().get(0)).isInstanceOf(NivelVo2Max.class);
        assertThat(((NivelVo2Max) tabela.getChildren().get(0)).getClassificacao()).isEqualTo("Ruim");
        assertThat(((NivelVo2Max) tabela.getChildren().get(1)).getClassificacao()).isEqualTo("Bom");
        assertThat(((NivelVo2Max) tabela.getChildren().get(2)).getClassificacao()).isEqualTo("Excelente");
    }

    @Test
    void shouldRoundTripTabelaSexoWithChildren() {
        var original = new TabelaSexo(Sexo.MASCULINO);
        original.add(new NivelForca("Forte", 50.0, 100.0));

        PersistedComponent persisted = mapper.toPersisted(original);
        Component result = mapper.toDomain(persisted);

        assertThat(result).isInstanceOf(TabelaSexo.class);
        var tabela = (TabelaSexo) result;
        assertThat(tabela.getSexo()).isEqualTo(Sexo.MASCULINO);
        assertThat(tabela.getChildren()).hasSize(1);
        assertThat(tabela.getChildren().get(0)).isInstanceOf(NivelForca.class);
    }

    @Test
    void shouldRoundTripTabelaEquipamento() {
        var original = new TabelaEquipamento();

        PersistedComponent persisted = mapper.toPersisted(original);
        Component result = mapper.toDomain(persisted);

        assertThat(result).isInstanceOf(TabelaEquipamento.class);
    }

    @Test
    void shouldRoundTripTabelaIdadeWithChildren() {
        var original = new TabelaIdade();
        original.setIdadeMin(18);
        original.setIdadeMax(30);
        original.add(new NivelForca("Jovem", 0.0, 50.0));

        PersistedComponent persisted = mapper.toPersisted(original);
        Component result = mapper.toDomain(persisted);

        assertThat(result).isInstanceOf(TabelaIdade.class);
        var tabela = (TabelaIdade) result;
        assertThat(tabela.getIdadeMin()).isEqualTo(18);
        assertThat(tabela.getIdadeMax()).isEqualTo(30);
        assertThat(tabela.getChildren()).hasSize(1);
        assertThat(tabela.getChildren().get(0)).isInstanceOf(NivelForca.class);
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toPersisted(null)).isNull();
    }

    @Test
    void shouldRoundTripNivelForcaComNomeENivel() {
        var original = new NivelForca("Força Absoluta", 5, "MUITO_BOM", 80.0, 100.0,
                TipoLimite.INCLUSIVO, TipoLimite.INCLUSIVO);

        PersistedComponent persisted = mapper.toPersisted(original);
        Component result = mapper.toDomain(persisted);

        assertThat(result).isInstanceOf(NivelForca.class);
        var nivel = (NivelForca) result;
        assertThat(nivel.getNome()).isEqualTo("Força Absoluta");
        assertThat(nivel.getNivel()).isEqualTo(5);
        assertThat(nivel.getClassificacao()).isEqualTo("MUITO_BOM");
        assertThat(nivel.getMin()).isEqualTo(80.0);
        assertThat(nivel.getMax()).isEqualTo(100.0);
    }

    @Test
    void shouldKeepDefaultTipoLimitesWhenPersistedHasNull() {
        var original = new NivelForca("Medio", 10.0, 20.0);
        original.setTipoMin(null);
        original.setTipoMax(null);

        PersistedComponent persisted = mapper.toPersisted(original);
        Component result = mapper.toDomain(persisted);

        assertThat(((NivelForca) result).getTipoMin()).isEqualTo(TipoLimite.INCLUSIVO);
        assertThat(((NivelForca) result).getTipoMax()).isEqualTo(TipoLimite.INCLUSIVO);
    }

    @Test
    void shouldBuildNestedTree() {
        var masculino = new TabelaSexo(Sexo.MASCULINO);
        masculino.add(new NivelForca("Baixo", 0.0, 30.0));
        masculino.add(new NivelForca("Alto", 30.0, 100.0));

        var feminino = new TabelaSexo(Sexo.FEMININO);
        feminino.add(new NivelForca("Baixo", 0.0, 25.0));
        feminino.add(new NivelForca("Alto", 25.0, 100.0));

        var raiz = new TabelaVo2Max();
        raiz.setProtocolo(Protocolo.COOPER);
        raiz.add(masculino);
        raiz.add(feminino);

        PersistedComponent persisted = mapper.toPersisted(raiz);
        Component result = mapper.toDomain(persisted);

        assertThat(result).isInstanceOf(TabelaVo2Max.class);
        var root = (Composite) result;
        assertThat(root.getChildren()).hasSize(2);
        assertThat(root.getChildren().get(0)).isInstanceOf(TabelaSexo.class);
        assertThat(((Composite) root.getChildren().get(0)).getChildren()).hasSize(2);
        assertThat(((Composite) root.getChildren().get(1)).getChildren()).hasSize(2);
    }
}
