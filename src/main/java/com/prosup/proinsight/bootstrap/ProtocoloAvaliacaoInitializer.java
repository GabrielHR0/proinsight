package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.config.properties.TabelaClassificacaoProperties;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
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
    private static final String STRATEGY_FUNCIONAL = "AVALIACAO_FUNCIONAL_IDOSO";

    private static final String CATEGORIA_VO2_MAX = "VO2_MAX";
    private static final String CATEGORIA_IMC = "IMC";
    private static final String CATEGORIA_FUNCIONAL = "FUNCIONAL";

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
        criarEsteiraIncremental();
        criarImc();
        criarAvaliacaoFuncionalIdoso();
    }

    private void criarCooper() {
        var doc = criarBasico("protocolo_vo2max_cooper", "Cooper 12 min",
            CATEGORIA_VO2_MAX, true, Protocolo.COOPER, STRATEGY_VO2_MAX,
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

    private void criarEsteiraIncremental() {
        var doc = criarBasico("protocolo_vo2max_esteira_incremental", "Esteira Incremental",
            CATEGORIA_VO2_MAX, false, Protocolo.ESTEIRA_INCREMENTAL,
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
            CATEGORIA_IMC, true, Protocolo.IMC, STRATEGY_IMC,
            tabelaProperties.getImcId());
        doc.setDescricao("Cálculo do Índice de Massa Corporal conforme classificação da OMS.");
        doc.setCalculadora("IMC = peso_kg / altura_m²");
        doc.setUnidadeMedida("kg/m²");
        doc.setEquipamentoNecessario("Balança calibrada, estadiômetro");
        repository.save(doc);
    }

    private void criarAvaliacaoFuncionalIdoso() {
        var doc = criarBasico("protocolo_avaliacao_funcional_idoso",
            "Avaliação Funcional do Idoso - Bateria de Fullerton",
            CATEGORIA_FUNCIONAL, true, Protocolo.AVALIACAO_FUNCIONAL_IDOSO,
            STRATEGY_FUNCIONAL, null);
        doc.setDescricao(
            "Bateria de testes de aptidão física para idosos proposta por Rikli e Jones (1999), " +
            "publicada em português como 'Teste de Aptidão Física para Idosos' (Manole, 2008). " +
            "Avalia força de membros inferiores e superiores, resistência aeróbia, flexibilidade de membros " +
            "inferiores e superiores, agilidade e equilíbrio dinâmico, com tabela de classificação própria por " +
            "sexo e faixa etária. A marcha estacionária de 2 minutos é usada como teste de resistência aeróbia " +
            "em lugar da caminhada de 6 minutos, por exigir menor espaço físico e maior praticidade, com boa " +
            "confiabilidade e reprodutibilidade em mulheres de 60 a 80 anos (Virtuoso Júnior e Guerra, 2011).");
        doc.setComoRealizar(
            "1. Sentar e levantar da cadeira em 30 segundos (força de membros inferiores): aluno sentado com pés " +
            "totalmente apoiados no chão e braços cruzados sobre o tórax; a cada sinal levantar totalmente e voltar " +
            "a sentar. Após demonstração e 3 práticas, pontuação = movimentos completos em 30 segundos.\n" +
            "2. Flexão de cotovelo em 30 segundos (força de membros superiores): aluno sentado com pés apoiados, " +
            "halter de 3 a 4 kg na mão ao longo do corpo e perpendicular ao chão; flexionar o cotovelo em amplitude " +
            "total o maior número de vezes em 30 segundos. Pontuação = número de flexões completas.\n" +
            "3. Marcha estacionária de 2 minutos (resistência aeróbia): marcar na parede a altura entre a patela e a " +
            "crista ilíaca do participante; ao sinal, marchar no lugar elevando um joelho de cada vez até a altura, " +
            "sem correr. Pontuação = passos em 2 minutos, contados a cada vez que o joelho direito atinge a marca.\n" +
            "4. Sentar e alcançar os pés (flexibilidade de membros inferiores): sentado na beirada da cadeira, perna " +
            "de melhor amplitude estendida à frente com calcanhar no chão, tornozelo a 90° e joelho estendido; mãos " +
            "sobrepostas alcançar o mais próximo possível da ponta do pé. Após 2 práticas, 2 tentativas (melhor " +
            "escore). Ponta do pé = zero: positivo se ultrapassar, negativo se não atingir, medido em cm.\n" +
            "5. Alcançar as costas (flexibilidade de membros superiores): uma das mãos passa sobre o ombro e a outra " +
            "por baixo, pelas costas; após definir em treino a posição preferida, 2 aquecimentos e 2 tentativas. " +
            "Medir em cm a distância entre os dedos médios: negativa se houver distância, positiva se se sobrepuserem. " +
            "Vale o melhor resultado.\n" +
            "6. Levantar e caminhar 2,5 metros (agilidade e equilíbrio dinâmico): aluno sentado no meio da cadeira, " +
            "mãos sobre as coxas, um pé ligeiramente à frente do outro e tronco levemente inclinado à frente; ao sinal, " +
            "levantar, contornar o cone a 2,5 metros e retornar a sentar. Cronômetro do sinal até sentar. Após 1 prática, " +
            "2 tentativas — melhor tempo, registrado em décimos de segundo (ex.: 4,54 segundos).");
        doc.setReferenciaBibliografica(
            "RIKLI, R. E.; JONES, C. J. Teste de Aptidão Física para Idosos: manual do instrutor. São Paulo: Manole, 2008.");
        doc.setEquipamentoNecessario(
            "Cadeira com 43 cm de altura sem apoios para os braços, cronômetro, halter de 3 a 4 kg, régua de 50 cm, " +
            "fita métrica, fita adesiva e cone");
        doc.setTabelasPorTeste(java.util.Map.of(
            TesteFuncionalTipo.SENTAR_LEVANTAR_30S.name(), tabelaProperties.getFullertonSentarLevantarId(),
            TesteFuncionalTipo.FLEXAO_COTOVELO_30S.name(), tabelaProperties.getFullertonFlexaoCotoveloId(),
            TesteFuncionalTipo.MARCHA_ESTACIONARIA_2MIN.name(), tabelaProperties.getFullertonMarcha2MinId(),
            TesteFuncionalTipo.SENTAR_ALCANCAR_PES.name(), tabelaProperties.getFullertonSentarAlcancarPesId(),
            TesteFuncionalTipo.ALCANCAR_COSTAS.name(), tabelaProperties.getFullertonAlcancarCostasId(),
            TesteFuncionalTipo.LEVANTAR_CAMINHAR_2M5.name(), tabelaProperties.getFullertonLevantarCaminharId()
        ));
        repository.save(doc);
    }

    private ProtocoloAvaliacaoDocument criarBasico(String id, String nome, String categoria,
                                                    Boolean padrao, Protocolo protocolo,
                                                    String strategyKey, String tabelaClassificacaoId) {
        if (repository.existsById(id)) {
            log.info("Protocolo '{}' já existe: {}", nome, id);
            var doc = repository.findById(id).orElseThrow();
            doc.setProtocolo(protocolo);
            doc.setStrategyKey(strategyKey);
            doc.setTabelaClassificacaoId(tabelaClassificacaoId);
            return doc;
        }
        return new ProtocoloAvaliacaoDocument(id, nome, categoria, padrao,
                protocolo, strategyKey, tabelaClassificacaoId);
    }
}
