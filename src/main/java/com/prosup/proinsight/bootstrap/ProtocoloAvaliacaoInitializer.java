package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.config.properties.TabelaClassificacaoProperties;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
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
    private static final String STRATEGY_VO2_MAX_ESTEIRA_INCREMENTAL = "VO2_MAX_ESTEIRA_INCREMENTAL";
    private static final String STRATEGY_IMC = "IMC";

    private static final String CATEGORIA_VO2_MAX = "VO2_MAX";
    private static final String CATEGORIA_IMC = "IMC";

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
        criarCooper();
        criarRockport();
        criarAha();
        criarEsteiraIncremental();
        criarImc();
    }

    private void criarCooper() {
        var doc = criarBasico("protocolo_vo2max_cooper", "Cooper 12 min",
            CATEGORIA_VO2_MAX, true, ProtocoloVo2Max.COOPER, STRATEGY_VO2_MAX,
            tabelaProperties.getCooperId());
        doc.setDescricao(
            "Teste de campo máximo em que o avaliado corre a maior distância possível em 12 minutos."
        );
        doc.setComoRealizar(
            "1. Aquecimento livre por 5-10 minutos.\n" +
            "2. O avaliado deve percorrer a maior distância possível em exatamente 12 minutos.\n" +
            "3. O avaliador marca a distância percorrida em metros ao final do tempo."
        );
        doc.setCalculadora("VO₂max (mL/kg/min) = (distanciaMetros - 504.9) / 44.73");
        doc.setReferenciaBibliografica("Cooper, K.H. (1968). JAMA, 203(3), 201–204.");
        doc.setUnidadeMedida("metros");
        doc.setTempoMinimoSegundos(720);
        doc.setTempoMaximoSegundos(720);
        doc.setEquipamentoNecessario("Cronômetro, marca de distância");
        repository.save(doc);
    }

    private void criarRockport() {
        var doc = criarBasico("protocolo_vo2max_rockport", "Rockport 1 mile",
            CATEGORIA_VO2_MAX, false, ProtocoloVo2Max.ROCKPORT, STRATEGY_VO2_MAX,
            tabelaProperties.getRockportId());
        doc.setDescricao(
            "Teste de campo submáximo — caminhar 1 milha o mais rápido possível."
        );
        doc.setComoRealizar(
            "1. Aquecimento leve por 5 minutos.\n" +
            "2. Caminhar 1 milha (1.609 km) no menor tempo possível.\n" +
            "3. Registrar tempo total e frequência cardíaca ao final."
        );
        doc.setCalculadora("VO₂max = 132.853 − (0.0769 × pesoLbs) − (0.3877 × idade) + (6.315 × sexo) − (3.2649 × tempo) − (0.1565 × FC)");
        doc.setReferenciaBibliografica("Kline, G.M. et al. (1987). Med Sci Sports Exerc, 19(3), 253–259.");
        doc.setUnidadeMedida("mL/kg/min");
        doc.setTempoMinimoSegundos(600);
        doc.setTempoMaximoSegundos(1800);
        doc.setEquipamentoNecessario("Cronômetro, oxímetro de pulso");
        repository.save(doc);
    }

    private void criarAha() {
        var doc = criarBasico("protocolo_vo2max_aha", "AHA/FRIEND (Esteira)",
            CATEGORIA_VO2_MAX, false, ProtocoloVo2Max.ESTEIRA, STRATEGY_VO2_MAX,
            tabelaProperties.getAhaId());
        doc.setDescricao("Protocolo de esteira submáximo/máximo com incrementos progressivos.");
        doc.setUnidadeMedida("mL/kg/min");
        doc.setEquipamentoNecessario("Esteira ergométrica, oxímetro de pulso");
        repository.save(doc);
    }

    private void criarEsteiraIncremental() {
        var doc = criarBasico("protocolo_vo2max_esteira_incremental", "Esteira Incremental",
            CATEGORIA_VO2_MAX, false, ProtocoloVo2Max.ESTEIRA_INCREMENTAL,
            STRATEGY_VO2_MAX_ESTEIRA_INCREMENTAL, tabelaProperties.getEsteiraIncrementalId());
        doc.setDescricao("Teste incremental adaptado — velocidades progressivas até VO₂max.");
        doc.setCalculadora("VO₂ = (0.2 × vel_m/min) + (0.9 × vel_m/min × inclinação/100) + 3.5");
        doc.setUnidadeMedida("mL/kg/min");
        doc.setTempoMinimoSegundos(180);
        doc.setTempoMaximoSegundos(1200);
        doc.setEquipamentoNecessario("Esteira com controle de velocidade");
        repository.save(doc);
    }

    private void criarImc() {
        var doc = criarBasico("protocolo_imc_oms", "IMC - OMS",
            CATEGORIA_IMC, true, null, STRATEGY_IMC,
            tabelaProperties.getImcId());
        doc.setDescricao("Cálculo do Índice de Massa Corporal conforme classificação da OMS.");
        doc.setCalculadora("IMC = peso_kg / altura_m²");
        doc.setUnidadeMedida("kg/m²");
        doc.setEquipamentoNecessario("Balança calibrada, estadiômetro");
        repository.save(doc);
    }

    private ProtocoloAvaliacaoDocument criarBasico(String id, String nome, String categoria,
                                                    Boolean padrao, ProtocoloVo2Max protocoloVo2Max,
                                                    String strategyKey, String tabelaClassificacaoId) {
        if (repository.existsById(id)) {
            log.info("Protocolo '{}' já existe: {}", nome, id);
            return repository.findById(id).orElseThrow();
        }
        return new ProtocoloAvaliacaoDocument(id, nome, categoria, padrao,
                protocoloVo2Max, strategyKey, tabelaClassificacaoId);
    }
}
