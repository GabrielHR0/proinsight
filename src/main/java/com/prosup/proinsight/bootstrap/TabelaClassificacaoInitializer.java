package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.config.properties.TabelaClassificacaoProperties;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedNivelVo2Max;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedTabelaIdade;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedTabelaSexo;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedTabelaVo2Max;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TabelaClassificacaoInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TabelaClassificacaoInitializer.class);

    private final TabelaClassificacaoRepository repository;
    private final TabelaClassificacaoProperties properties;

    public TabelaClassificacaoInitializer(
        TabelaClassificacaoRepository repository,
        TabelaClassificacaoProperties properties
    ) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        if (repository.existsById(properties.getCooperId())) {
            log.info("Tabela Cooper já existe: {}", properties.getCooperId());
        } else {
            repository.save(criarTabelaCooper());
            log.info("Tabela Cooper criada: {}", properties.getCooperId());
        }

        if (repository.existsById(properties.getRockportId())) {
            log.info("Tabela Rockport já existe: {}", properties.getRockportId());
        } else {
            repository.save(criarTabelaRockport());
            log.info("Tabela Rockport criada: {}", properties.getRockportId());
        }
    }

    private TabelaClassificacaoDocument criarTabelaCooper() {
        var raiz = new PersistedTabelaVo2Max();
        raiz.setProtocolo(ProtocoloVo2Max.COOPER);

        var masculino = new PersistedTabelaSexo(Sexo.MASCULINO);
        masculino.addComponente(criarFaixaEtariaCooper(20, 29));
        raiz.addComponente(masculino);

        var feminino = new PersistedTabelaSexo(Sexo.FEMININO);
        raiz.addComponente(feminino);

        var doc = new TabelaClassificacaoDocument(properties.getCooperId(), "Classificação Cooper 12 min");
        doc.setRaiz(raiz);
        return doc;
    }

    private PersistedTabelaIdade criarFaixaEtariaCooper(int idadeMin, int idadeMax) {
        var idade = new PersistedTabelaIdade(idadeMin, idadeMax);
        idade.addComponente(new PersistedNivelVo2Max("RUIM", null, 1600.0, null, TipoLimite.EXCLUSIVO));
        idade.addComponente(new PersistedNivelVo2Max("ABAIXO", 1600.0, 2199.0));
        idade.addComponente(new PersistedNivelVo2Max("MÉDIO", 2200.0, 2399.0));
        idade.addComponente(new PersistedNivelVo2Max("BOM", 2400.0, 2800.0));
        idade.addComponente(new PersistedNivelVo2Max("EXCELENTE", 2800.0, null, TipoLimite.EXCLUSIVO, null));
        return idade;
    }

    private TabelaClassificacaoDocument criarTabelaRockport() {
        var raiz = new PersistedTabelaVo2Max();
        raiz.setProtocolo(ProtocoloVo2Max.ROCKPORT);

        var masculino = new PersistedTabelaSexo(Sexo.MASCULINO);
        masculino.addComponente(criarFaixaEtariaRockport(20, 29));
        raiz.addComponente(masculino);

        var feminino = new PersistedTabelaSexo(Sexo.FEMININO);
        raiz.addComponente(feminino);

        var doc = new TabelaClassificacaoDocument(properties.getRockportId(), "Classificação Rockport 1 mile");
        doc.setRaiz(raiz);
        return doc;
    }

    private PersistedTabelaIdade criarFaixaEtariaRockport(int idadeMin, int idadeMax) {
        var idade = new PersistedTabelaIdade(idadeMin, idadeMax);
        idade.addComponente(new PersistedNivelVo2Max("RUIM", null, 41.7, null, TipoLimite.EXCLUSIVO));
        idade.addComponente(new PersistedNivelVo2Max("RAZOÁVEL", 41.7, 45.4));
        idade.addComponente(new PersistedNivelVo2Max("BOM", 45.4, 51.1));
        idade.addComponente(new PersistedNivelVo2Max("EXCELENTE", 51.1, 55.4));
        idade.addComponente(new PersistedNivelVo2Max("SUPERIOR", 55.4, null, TipoLimite.EXCLUSIVO, null));
        return idade;
    }
}
