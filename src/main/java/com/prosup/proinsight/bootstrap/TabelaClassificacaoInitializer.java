package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.config.properties.TabelaClassificacaoProperties;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedNivelImc;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedNivelVo2Max;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedTabelaClassificacaoGenerica;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedTabelaIdade;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedTabelaSexo;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedTabelaVo2Max;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComposite;
import java.util.function.Supplier;

@Component
public class    TabelaClassificacaoInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TabelaClassificacaoInitializer.class);

    private record Faixa(int idadeMin, int idadeMax, double tMuitoRuim, double tRuim, double tMedio, double tBom) {}

    private static final Faixa[] COOPER_MASC = {
        new Faixa(20, 29, 1600, 2200, 2400, 2800),
        new Faixa(30, 39, 1500, 1900, 2300, 2700),
        new Faixa(40, 49, 1400, 1700, 2100, 2500),
        new Faixa(50, 59, 1300, 1600, 2000, 2400),
        new Faixa(60, 99, 1200, 1500, 1900, 2300),
    };

    private static final Faixa[] COOPER_FEM = {
        new Faixa(20, 29, 1500, 1800, 2100, 2300),
        new Faixa(30, 39, 1400, 1700, 2000, 2200),
        new Faixa(40, 49, 1200, 1500, 1800, 2100),
        new Faixa(50, 59, 1100, 1400, 1700, 2000),
        new Faixa(60, 99, 1000, 1300, 1600, 1900),
    };

    private static final Faixa[] ROCKPORT_MASC = {
        new Faixa(20, 29, 33, 37, 42, 46),
        new Faixa(30, 39, 31, 35, 40, 44),
        new Faixa(40, 49, 28, 33, 37, 42),
        new Faixa(50, 59, 25, 29, 34, 38),
        new Faixa(60, 99, 22, 26, 31, 35),
    };

    private static final Faixa[] ROCKPORT_FEM = {
        new Faixa(20, 29, 24, 29, 33, 37),
        new Faixa(30, 39, 22, 27, 31, 35),
        new Faixa(40, 49, 20, 25, 29, 33),
        new Faixa(50, 59, 18, 22, 26, 30),
        new Faixa(60, 99, 16, 20, 24, 28),
    };

    private static final Faixa[] AHA_MASC = {
        new Faixa(20, 29, 35, 44, 49, 55),
        new Faixa(30, 39, 30, 37, 43, 50),
        new Faixa(40, 49, 27, 32, 38, 45),
        new Faixa(50, 59, 22, 27, 32, 38),
        new Faixa(60, 69, 19, 23, 27, 32),
        new Faixa(70, 79, 16, 19, 22, 26),
        new Faixa(80, 99, 15, 17, 18, 21),
    };

    private static final Faixa[] AHA_FEM = {
        new Faixa(20, 29, 27, 34, 39, 45),
        new Faixa(30, 39, 22, 26, 31, 37),
        new Faixa(40, 49, 20, 24, 28, 33),
        new Faixa(50, 59, 19, 22, 25, 28),
        new Faixa(60, 69, 15, 18, 21, 24),
        new Faixa(70, 79, 14, 16, 18, 21),
        new Faixa(80, 99, 13, 15, 16, 18),
    };

    private static final PersistedNivelImc[] FAIXAS_IMC = {
        new PersistedNivelImc("ABAIXO_DO_PESO", null, 18.5, null, TipoLimite.EXCLUSIVO),
        new PersistedNivelImc("NORMAL", 18.5, 25.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO),
        new PersistedNivelImc("SOBREPESO", 25.0, 30.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO),
        new PersistedNivelImc("OBESIDADE_I", 30.0, 35.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO),
        new PersistedNivelImc("OBESIDADE_II", 35.0, 40.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO),
        new PersistedNivelImc("OBESIDADE_III", 40.0, null, TipoLimite.INCLUSIVO, null),
    };

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
        criar(properties.getCooperId(), "Classificação Cooper 12 min", this::criarRaizCooper);
        criar(properties.getRockportId(), "Classificação Rockport 1 mile", this::criarRaizRockport);
        criar(properties.getAhaId(), "Classificação VO₂ Máx - AHA/FRIEND", this::criarRaizAha);
        criar(properties.getEsteiraIncrementalId(), "Classificação VO₂ Máx - Esteira Incremental (ACSM/AHA)", this::criarRaizEsteiraIncremental);
        criar(properties.getImcId(), "Classificação IMC - OMS", this::criarRaizImc);
    }

    private void criar(String id, String descricao, Supplier<? extends PersistedComposite> raizSupplier) {
        if (repository.existsById(id)) {
            log.info("Tabela '{}' já existe: {}", descricao, id);
            return;
        }
        var doc = new TabelaClassificacaoDocument(id, descricao);
        doc.setRaiz(raizSupplier.get());
        repository.save(doc);
        log.info("Tabela '{}' criada: {}", descricao, id);
    }

    private PersistedTabelaVo2Max criarRaizCooper() {
        var raiz = new PersistedTabelaVo2Max();
        raiz.setProtocolo(Protocolo.COOPER);
        preencherSexos(raiz, COOPER_MASC, COOPER_FEM);
        return raiz;
    }

    private PersistedTabelaVo2Max criarRaizRockport() {
        var raiz = new PersistedTabelaVo2Max();
        raiz.setProtocolo(Protocolo.ROCKPORT);
        preencherSexos(raiz, ROCKPORT_MASC, ROCKPORT_FEM);
        return raiz;
    }

    private PersistedTabelaVo2Max criarRaizAha() {
        var raiz = new PersistedTabelaVo2Max();
        preencherSexos(raiz, AHA_MASC, AHA_FEM);
        return raiz;
    }

    private PersistedTabelaClassificacaoGenerica criarRaizEsteiraIncremental() {
        var raiz = new PersistedTabelaClassificacaoGenerica();
        preencherSexosGenerico(raiz, AHA_MASC, AHA_FEM);
        return raiz;
    }

    private PersistedTabelaClassificacaoGenerica criarRaizImc() {
        var raiz = new PersistedTabelaClassificacaoGenerica();
        for (var faixa : FAIXAS_IMC) {
            raiz.addComponente(faixa);
        }
        return raiz;
    }

    private static void preencherSexosGenerico(PersistedTabelaClassificacaoGenerica raiz, Faixa[] masculino, Faixa[] feminino) {
        var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
        for (var f : masculino) masc.addComponente(criarFaixa(f));
        raiz.addComponente(masc);

        var fem = new PersistedTabelaSexo(Sexo.FEMININO);
        for (var f : feminino) fem.addComponente(criarFaixa(f));
        raiz.addComponente(fem);
    }

    private static void preencherSexos(PersistedTabelaVo2Max raiz, Faixa[] masculino, Faixa[] feminino) {
        var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
        for (var f : masculino) masc.addComponente(criarFaixa(f));
        raiz.addComponente(masc);

        var fem = new PersistedTabelaSexo(Sexo.FEMININO);
        for (var f : feminino) fem.addComponente(criarFaixa(f));
        raiz.addComponente(fem);
    }

    private static PersistedTabelaIdade criarFaixa(Faixa f) {
        var idade = new PersistedTabelaIdade(f.idadeMin, f.idadeMax);
        idade.addComponente(new PersistedNivelVo2Max("MUITO_RUIM", null, f.tMuitoRuim, null, TipoLimite.EXCLUSIVO));
        idade.addComponente(new PersistedNivelVo2Max("RUIM", f.tMuitoRuim, f.tRuim, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        idade.addComponente(new PersistedNivelVo2Max("MÉDIO", f.tRuim, f.tMedio, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        idade.addComponente(new PersistedNivelVo2Max("BOM", f.tMedio, f.tBom, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        idade.addComponente(new PersistedNivelVo2Max("EXCELENTE", f.tBom, null, TipoLimite.INCLUSIVO, null));
        return idade;
    }

}
