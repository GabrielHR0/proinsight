package com.prosup.proinsight.infrastructure.persistence;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.classes.NivelForca;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaVo2Max;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.PersistedComponentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class TabelaClassificacaoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PersistedComponentMapper componentMapper;

    @Test
    void shouldPersistAndRetrieveDomainTree() {
        var masculino = new TabelaSexo(Sexo.MASCULINO);
        masculino.add(new NivelForca("Baixo", 0.0, 30.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        masculino.add(new NivelForca("Alto", 30.0, 100.0, TipoLimite.INCLUSIVO, TipoLimite.INCLUSIVO));

        var feminino = new TabelaSexo(Sexo.FEMININO);
        feminino.add(new NivelForca("Baixo", 0.0, 25.0));
        feminino.add(new NivelForca("Alto", 25.0, 100.0));

        var raiz = new TabelaVo2Max();
        raiz.setProtocolo(Protocolo.COOPER);
        raiz.add(masculino);
        raiz.add(feminino);

        var persistedRaiz = componentMapper.toPersisted(raiz);

        var doc = new TabelaClassificacaoDocument();
        doc.setNome("test-integration-domain-tree");
        doc.setRaiz(persistedRaiz);

        TabelaClassificacaoDocument saved = mongoTemplate.save(doc);
        TabelaClassificacaoDocument loaded = mongoTemplate.findById(saved.getId(), TabelaClassificacaoDocument.class);

        assertThat(loaded).isNotNull();

        Component result = componentMapper.toDomain(loaded.getRaiz());

        assertThat(result).isInstanceOf(TabelaVo2Max.class);
        var root = (Composite) result;
        assertThat(root.getChildren()).hasSize(2);
        assertThat(root.getChildren().get(0)).isInstanceOf(TabelaSexo.class);
        assertThat(root.getChildren().get(1)).isInstanceOf(TabelaSexo.class);

        var sexoMasc = (TabelaSexo) root.getChildren().get(0);
        assertThat(sexoMasc.getSexo()).isEqualTo(Sexo.MASCULINO);
        assertThat(sexoMasc.getChildren()).hasSize(2);
        assertThat(sexoMasc.getChildren().get(0)).isInstanceOf(NivelForca.class);

        var nivel = (NivelForca) sexoMasc.getChildren().get(0);
        assertThat(nivel.getClassificacao()).isEqualTo("Baixo");
        assertThat(nivel.getMin()).isEqualTo(0.0);
        assertThat(nivel.getMax()).isEqualTo(30.0);
        assertThat(nivel.getTipoMin()).isEqualTo(TipoLimite.INCLUSIVO);
        assertThat(nivel.getTipoMax()).isEqualTo(TipoLimite.EXCLUSIVO);

        var nivelAlto = (NivelForca) sexoMasc.getChildren().get(1);
        assertThat(nivelAlto.getClassificacao()).isEqualTo("Alto");
        assertThat(nivelAlto.getMin()).isEqualTo(30.0);
        assertThat(nivelAlto.getMax()).isEqualTo(100.0);

        var sexoFem = (TabelaSexo) root.getChildren().get(1);
        assertThat(sexoFem.getSexo()).isEqualTo(Sexo.FEMININO);
        assertThat(sexoFem.getChildren()).hasSize(2);
    }

    @Test
    void shouldPersistAndRetrieveNivelVo2MaxTree() {
        var raiz = new TabelaVo2Max();
        raiz.setProtocolo(Protocolo.COOPER);
        raiz.add(new NivelVo2Max("Ruim", 0.0, 25.0));
        raiz.add(new NivelVo2Max("Bom", 25.0, 40.0));
        raiz.add(new NivelVo2Max("Excelente", 40.0, 100.0));

        var persistedRaiz = componentMapper.toPersisted(raiz);

        var doc = new TabelaClassificacaoDocument();
        doc.setNome("test-vo2max-levels");
        doc.setRaiz(persistedRaiz);

        TabelaClassificacaoDocument saved = mongoTemplate.save(doc);
        Component result = componentMapper.toDomain(
                mongoTemplate.findById(saved.getId(), TabelaClassificacaoDocument.class).getRaiz()
        );

        assertThat(result).isInstanceOf(TabelaVo2Max.class);
        assertThat(((Composite) result).getChildren()).hasSize(3);
        assertThat(((NivelVo2Max) ((Composite) result).getChildren().get(2)).getClassificacao())
                .isEqualTo("Excelente");
    }

    @Test
    void shouldPersistAndRetrieveLeafOnlyTree() {
        var raiz = new NivelForca("Unico", 10.0, 20.0);

        var persistedRaiz = componentMapper.toPersisted(raiz);

        var doc = new TabelaClassificacaoDocument();
        doc.setNome("test-leaf-only");
        doc.setRaiz(persistedRaiz);

        TabelaClassificacaoDocument saved = mongoTemplate.save(doc);
        Component result = componentMapper.toDomain(
                mongoTemplate.findById(saved.getId(), TabelaClassificacaoDocument.class).getRaiz()
        );

        assertThat(result).isInstanceOf(NivelForca.class);
        assertThat(((NivelForca) result).getClassificacao()).isEqualTo("Unico");
        assertThat(((NivelForca) result).getMin()).isEqualTo(10.0);
        assertThat(((NivelForca) result).getMax()).isEqualTo(20.0);
    }

    @Test
    void shouldPersistAndRetrieveNivelForcaComNomeENivel() {
        var raiz = new NivelForca("Nível Excelente", 3, "EXCELENTE", 40.0, 100.0,
                TipoLimite.INCLUSIVO, TipoLimite.INCLUSIVO);

        var persistedRaiz = componentMapper.toPersisted(raiz);

        var doc = new TabelaClassificacaoDocument();
        doc.setNome("test-nivel-forca-com-nome");
        doc.setRaiz(persistedRaiz);

        TabelaClassificacaoDocument saved = mongoTemplate.save(doc);
        Component result = componentMapper.toDomain(
                mongoTemplate.findById(saved.getId(), TabelaClassificacaoDocument.class).getRaiz()
        );

        assertThat(result).isInstanceOf(NivelForca.class);
        var nivel = (NivelForca) result;
        assertThat(nivel.getNome()).isEqualTo("Nível Excelente");
        assertThat(nivel.getNivel()).isEqualTo(3);
        assertThat(nivel.getClassificacao()).isEqualTo("EXCELENTE");
        assertThat(nivel.getMin()).isEqualTo(40.0);
        assertThat(nivel.getMax()).isEqualTo(100.0);
    }
}
