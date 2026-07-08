package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.config.properties.TabelaClassificacaoProperties;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProtocoloAvaliacaoInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProtocoloAvaliacaoInitializer.class);

    private static final String STRATEGY_VO2_MAX = "VO2_MAX";

    private final ProtocoloAvaliacaoRepository repository;
    private final TabelaClassificacaoProperties tabelaProperties;

    public ProtocoloAvaliacaoInitializer(
        ProtocoloAvaliacaoRepository repository,
        TabelaClassificacaoProperties tabelaProperties
    ) {
        this.repository = repository;
        this.tabelaProperties = tabelaProperties;
    }

    @Override
    public void run(String... args) {
        criar("protocolo_vo2max_cooper", "Avaliação VO2 Máx - Cooper 12 min",
              STRATEGY_VO2_MAX, tabelaProperties.getCooperId());
        criar("protocolo_vo2max_rockport", "Avaliação VO2 Máx - Rockport 1 mile",
              STRATEGY_VO2_MAX, tabelaProperties.getRockportId());
        criar("protocolo_vo2max_aha", "Avaliação VO2 Máx - AHA/FRIEND (Esteira)",
              STRATEGY_VO2_MAX, tabelaProperties.getAhaId());
    }

    private void criar(String id, String nome, String strategyKey, String tabelaClassificacaoId) {
        if (repository.existsById(id)) {
            log.info("Protocolo '{}' já existe: {}", nome, id);
            return;
        }
        var doc = new ProtocoloAvaliacaoDocument(id, nome, strategyKey, tabelaClassificacaoId);
        repository.save(doc);
        log.info("Protocolo '{}' criado: {}", nome, id);
    }
}
