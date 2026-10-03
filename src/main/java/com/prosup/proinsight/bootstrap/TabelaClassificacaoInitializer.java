package com.prosup.proinsight.bootstrap;

import com.prosup.proinsight.config.properties.TabelaClassificacaoProperties;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedNivelImc;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedNivelVo2Max;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedPercentilFuncional;
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

    private static final int[] PERCENTIS_FULLERTON = {
        95, 90, 85, 80, 75, 70, 65, 60, 55, 50, 45, 40, 35, 30, 25, 20, 15, 10, 5
    };

    private record FaixaPercentil(int idadeMin, int idadeMax, double[] cortes) {}

    private static final FaixaPercentil[] FULLERTON_SENTAR_LEVANTAR_MASC_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{23, 22, 21, 20, 19, 19, 18, 17, 17, 16, 16, 15, 15, 14, 14, 13, 12, 11, 9}),
        new FaixaPercentil(65, 69, new double[]{23, 21, 20, 19, 18, 18, 17, 16, 16, 15, 15, 14, 13, 13, 12, 11, 11, 9, 8}),
        new FaixaPercentil(70, 74, new double[]{21, 20, 19, 18, 17, 17, 16, 16, 15, 14, 14, 13, 13, 12, 12, 11, 10, 9, 8}),
        new FaixaPercentil(75, 79, new double[]{21, 20, 18, 18, 17, 16, 16, 15, 15, 14, 13, 13, 12, 12, 11, 10, 10, 8, 7}),
        new FaixaPercentil(80, 84, new double[]{19, 17, 16, 16, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10, 10, 9, 8, 7, 6}),
        new FaixaPercentil(85, 89, new double[]{19, 17, 16, 15, 14, 13, 13, 12, 12, 11, 11, 10, 9, 9, 8, 7, 6, 5, 4}),
        new FaixaPercentil(90, 94, new double[]{16, 15, 14, 13, 12, 12, 11, 11, 10, 10, 9, 9, 8, 8, 7, 7, 6, 5, 3}),
    };

    private static final FaixaPercentil[] FULLERTON_SENTAR_LEVANTAR_FEM_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{21, 20, 19, 18, 17, 17, 16, 16, 15, 15, 14, 14, 13, 12, 12, 11, 10, 9, 8}),
        new FaixaPercentil(65, 69, new double[]{19, 18, 17, 16, 16, 15, 15, 14, 14, 14, 13, 13, 12, 12, 11, 11, 10, 9, 8}),
        new FaixaPercentil(70, 74, new double[]{19, 18, 17, 16, 15, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10, 10, 9, 8, 7}),
        new FaixaPercentil(75, 79, new double[]{19, 17, 16, 16, 15, 14, 14, 13, 13, 12, 12, 12, 11, 11, 10, 9, 9, 8, 6}),
        new FaixaPercentil(80, 84, new double[]{18, 17, 16, 15, 14, 13, 13, 12, 12, 11, 11, 10, 10, 9, 9, 8, 7, 6, 4}),
        new FaixaPercentil(85, 89, new double[]{17, 15, 14, 14, 13, 12, 12, 11, 11, 10, 10, 9, 9, 8, 8, 7, 6, 5, 4}),
        new FaixaPercentil(90, 94, new double[]{16, 15, 13, 12, 11, 11, 10, 9, 9, 8, 7, 7, 6, 5, 4, 4, 3, 1, 0}),
    };

    private static final FaixaPercentil[] FULLERTON_FLEXAO_MASC_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{27, 25, 24, 23, 22, 21, 21, 20, 20, 19, 18, 18, 17, 17, 16, 15, 14, 13, 11}),
        new FaixaPercentil(65, 69, new double[]{27, 25, 24, 23, 21, 21, 20, 20, 19, 18, 18, 17, 16, 16, 15, 14, 13, 12, 10}),
        new FaixaPercentil(70, 74, new double[]{26, 24, 23, 22, 21, 20, 19, 19, 18, 17, 17, 16, 15, 15, 14, 13, 12, 11, 9}),
        new FaixaPercentil(75, 79, new double[]{24, 22, 21, 20, 19, 19, 18, 17, 17, 16, 16, 15, 14, 14, 13, 12, 11, 10, 9}),
        new FaixaPercentil(80, 84, new double[]{23, 22, 20, 20, 19, 18, 18, 17, 17, 16, 15, 15, 14, 14, 13, 12, 12, 10, 9}),
        new FaixaPercentil(85, 89, new double[]{21, 19, 18, 17, 17, 16, 15, 15, 14, 14, 13, 13, 12, 11, 11, 10, 9, 8, 7}),
        new FaixaPercentil(90, 94, new double[]{18, 16, 16, 15, 14, 14, 13, 13, 12, 12, 12, 11, 11, 10, 10, 9, 8, 8, 6}),
    };
    private static final FaixaPercentil[] FULLERTON_FLEXAO_FEM_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{24, 22, 21, 20, 19, 18, 18, 17, 17, 16, 16, 15, 14, 14, 13, 12, 11, 10, 9}),
        new FaixaPercentil(65, 69, new double[]{22, 21, 20, 19, 18, 17, 17, 16, 16, 15, 15, 14, 14, 13, 12, 12, 11, 10, 8}),
        new FaixaPercentil(70, 74, new double[]{22, 20, 19, 18, 17, 17, 16, 16, 15, 14, 14, 13, 13, 12, 12, 11, 10, 9, 8}),
        new FaixaPercentil(75, 79, new double[]{21, 20, 19, 18, 17, 16, 16, 15, 15, 14, 13, 13, 12, 12, 11, 10, 9, 8, 7}),
        new FaixaPercentil(80, 84, new double[]{20, 18, 17, 16, 16, 15, 15, 14, 14, 13, 12, 12, 11, 11, 10, 10, 9, 8, 6}),
        new FaixaPercentil(85, 89, new double[]{18, 17, 16, 15, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10, 10, 9, 8, 7, 6}),
        new FaixaPercentil(90, 94, new double[]{17, 16, 15, 15, 13, 13, 12, 12, 11, 11, 10, 10, 9, 9, 8, 8, 7, 6, 5}),
    };

    private static final FaixaPercentil[] FULLERTON_MARCHA_MASC_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{135, 128, 123, 119, 115, 112, 109, 106, 104, 101, 98, 96, 93, 90, 87, 83, 79, 74, 67}),
        new FaixaPercentil(65, 69, new double[]{139, 130, 125, 120, 116, 113, 110, 107, 104, 101, 98, 95, 92, 89, 86, 82, 77, 72, 67}),
        new FaixaPercentil(70, 74, new double[]{133, 124, 119, 114, 110, 107, 104, 101, 98, 95, 92, 89, 86, 83, 80, 76, 71, 67, 66}),
        new FaixaPercentil(75, 79, new double[]{135, 126, 119, 114, 109, 105, 102, 98, 95, 91, 87, 84, 80, 77, 73, 68, 63, 56, 47}),
        new FaixaPercentil(80, 84, new double[]{126, 118, 112, 107, 103, 99, 96, 93, 90, 87, 84, 81, 78, 75, 71, 67, 62, 56, 48}),
        new FaixaPercentil(85, 89, new double[]{114, 106, 100, 95, 91, 87, 84, 81, 78, 75, 72, 69, 66, 63, 59, 55, 50, 44, 36}),
        new FaixaPercentil(90, 94, new double[]{112, 102, 96, 91, 86, 83, 79, 76, 72, 69, 66, 62, 59, 55, 52, 47, 42, 36, 26}),
    };
    private static final FaixaPercentil[] FULLERTON_MARCHA_FEM_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{130, 122, 116, 112, 107, 103, 100, 97, 94, 91, 88, 85, 82, 79, 75, 71, 66, 60, 52}),
        new FaixaPercentil(65, 69, new double[]{133, 123, 117, 112, 107, 104, 100, 96, 93, 90, 87, 84, 80, 76, 73, 68, 63, 57, 47}),
        new FaixaPercentil(70, 74, new double[]{125, 116, 110, 105, 101, 97, 94, 90, 87, 84, 81, 78, 74, 71, 68, 63, 58, 52, 43}),
        new FaixaPercentil(75, 79, new double[]{123, 115, 109, 104, 100, 96, 93, 90, 87, 84, 81, 78, 75, 72, 68, 64, 59, 53, 45}),
        new FaixaPercentil(80, 84, new double[]{113, 104, 99, 94, 90, 87, 84, 81, 78, 75, 72, 69, 66, 63, 60, 56, 51, 46, 37}),
        new FaixaPercentil(85, 89, new double[]{106, 98, 93, 88, 85, 81, 79, 76, 73, 70, 67, 64, 61, 59, 55, 52, 47, 42, 39}),
        new FaixaPercentil(90, 94, new double[]{92, 85, 80, 76, 72, 69, 66, 63, 61, 58, 55, 53, 50, 47, 44, 40, 36, 31, 24}),
    };

    private static final FaixaPercentil[] FULLERTON_SENTAR_ALCANCAR_MASC_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{8.5, 6.7, 5.6, 4.6, 3.8, 3.1, 2.5, 1.8, 1.2, 0.6, 0.0, -0.6, -1.3, -1.9, -2.6, -3.4, -4.4, -5.5, -7.3}),
        new FaixaPercentil(65, 69, new double[]{7.5, 5.9, 4.8, 3.9, 3.1, 2.4, 1.8, 1.1, 0.6, 0.0, -0.6, -1.1, -1.8, -2.4, -3.1, -3.9, -4.8, -5.9, -7.5}),
        new FaixaPercentil(70, 74, new double[]{7.5, 5.8, 4.7, 3.8, 3.0, 2.4, 1.8, 1.1, 0.6, 0.0, -0.6, -1.2, -1.8, -2.4, -3.1, -3.9, -4.8, -5.9, -7.6}),
        new FaixaPercentil(75, 79, new double[]{6.6, 4.9, 3.8, 2.8, 2.0, 1.3, 0.7, 0.1, -0.5, -1.1, -1.7, -2.3, -2.9, -3.5, -4.2, -5.0, -6.0, -7.1, -8.8}),
        new FaixaPercentil(80, 84, new double[]{6.2, 4.4, 3.2, 2.2, 1.4, 0.6, 0.0, -0.8, -1.4, -2.0, -2.6, -3.2, -4.0, -4.6, -5.3, -6.2, -7.2, -8.4, -10.2}),
        new FaixaPercentil(85, 89, new double[]{4.5, 3.0, 2.0, 1.1, 0.4, -0.2, -0.8, -1.3, -1.9, -2.4, -2.9, -3.5, -4.0, -4.6, -5.3, -5.9, -6.8, -7.8, -9.3}),
        new FaixaPercentil(90, 94, new double[]{3.5, 1.9, 0.9, 0.0, -0.7, -1.4, -1.9, -2.5, -3.0, -3.6, -4.2, -4.7, -5.3, -5.8, -6.5, -7.2, -8.1, -9.1, -10.7}),
    };
    private static final FaixaPercentil[] FULLERTON_SENTAR_ALCANCAR_FEM_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{8.7, 7.2, 6.3, 5.5, 4.8, 4.2, 3.7, 3.1, 2.6, 2.1, 1.6, 1.1, 0.5, 0.0, -0.6, -1.3, -2.1, -3.0, -4.0}),
        new FaixaPercentil(65, 69, new double[]{7.9, 6.6, 5.7, 5.0, 4.4, 3.9, 3.4, 2.9, 2.5, 2.0, 1.5, 1.1, 0.6, 0.1, -0.4, -1.0, -1.7, -2.6, -3.9}),
        new FaixaPercentil(70, 74, new double[]{7.5, 6.1, 5.2, 4.5, 3.9, 3.3, 2.8, 2.3, 1.9, 1.4, 0.9, 0.5, 0.0, -0.5, -1.1, -1.7, -2.4, -3.3, -4.7}),
        new FaixaPercentil(75, 79, new double[]{7.4, 6.1, 5.2, 4.4, 3.7, 3.2, 2.7, 2.1, 1.7, 1.2, 0.7, 0.2, -0.3, -0.8, -1.3, -2.0, -2.8, -3.7, -5.0}),
        new FaixaPercentil(80, 84, new double[]{6.6, 5.2, 4.3, 3.6, 3.0, 2.4, 1.9, 1.4, 1.0, 0.5, 0.0, -0.4, -0.9, -1.4, -2.0, -2.6, -3.3, -4.2, -5.0}),
        new FaixaPercentil(85, 89, new double[]{6.0, 4.6, 3.7, 3.0, 2.4, 1.8, 1.3, 0.8, 0.4, -0.1, -0.6, -1.0, -1.5, -2.0, -2.6, -3.2, -3.9, -4.8, -6.3}),
        new FaixaPercentil(90, 94, new double[]{4.9, 3.4, 2.5, 1.7, 1.0, 0.4, -0.1, -0.7, -1.2, -1.7, -2.2, -2.7, -3.3, -3.8, -4.4, -5.1, -5.9, -6.8, -7.9}),
    };

    private static final FaixaPercentil[] FULLERTON_ALCANCAR_COSTAS_MASC_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{4.5, 2.7, 1.6, 0.6, -0.2, -0.9, -1.5, -2.2, -2.8, -3.4, -4.0, -4.6, -5.3, -5.9, -6.6, -7.4, -8.4, -9.5, -11.3}),
        new FaixaPercentil(65, 69, new double[]{3.9, 2.2, 1.0, 0.0, -0.8, -1.6, -2.2, -2.9, -3.5, -4.1, -4.7, -5.3, -6.0, -6.6, -7.4, -8.2, -9.2, -10.4, -12.1}),
        new FaixaPercentil(70, 74, new double[]{3.5, 1.8, 0.6, -0.4, -1.2, -2.0, -2.6, -3.3, -3.9, -4.5, -5.1, -5.7, -6.4, -7.0, -7.8, -8.6, -9.6, -10.8, -12.5}),
        new FaixaPercentil(75, 79, new double[]{2.8, 0.9, -0.3, -1.3, -2.2, -2.9, -3.6, -4.3, -4.9, -5.6, -6.3, -6.9, -7.6, -8.3, -9.0, -9.9, -10.9, -12.1, -14.0}),
        new FaixaPercentil(80, 84, new double[]{3.2, 1.2, -0.1, -1.2, -2.1, -2.9, -3.6, -4.3, -5.0, -5.7, -6.4, -7.1, -7.8, -8.5, -9.3, -10.2, -11.3, -12.6, -14.6}),
        new FaixaPercentil(85, 89, new double[]{1.7, -0.1, -1.2, -2.2, -3.0, -3.7, -4.3, -5.0, -5.6, -6.2, -6.8, -7.4, -8.1, -8.7, -9.4, -10.2, -11.2, -12.3, -14.1}),
        new FaixaPercentil(90, 94, new double[]{0.7, -1.1, -2.2, -3.2, -4.0, -4.7, -5.3, -6.0, -6.6, -7.2, -7.8, -8.4, -9.1, -9.7, -10.4, -11.2, -12.2, -13.3, -15.1}),
    };
    private static final FaixaPercentil[] FULLERTON_ALCANCAR_COSTAS_FEM_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{5.0, 3.8, 2.9, 2.2, 1.6, 1.1, 0.7, 0.2, -0.2, -0.7, -1.2, -1.6, -2.1, -2.5, -3.0, -3.6, -4.3, -5.2, -6.4}),
        new FaixaPercentil(65, 69, new double[]{4.9, 3.5, 2.6, 1.9, 1.3, 0.7, 0.2, -0.3, -0.7, -1.2, -1.7, -2.1, -2.6, -3.1, -3.7, -4.3, -5.0, -5.9, -7.3}),
        new FaixaPercentil(70, 74, new double[]{4.5, 3.2, 2.3, 1.5, 0.8, 0.3, -0.2, -0.8, -1.2, -1.7, -2.2, -2.6, -3.2, -3.7, -4.2, -4.9, -5.7, -6.6, -7.9}),
        new FaixaPercentil(75, 79, new double[]{4.5, 3.1, 2.2, 1.3, 0.6, 0.0, -0.5, -1.1, -1.6, -2.1, -2.6, -3.1, -3.7, -4.2, -4.8, -5.5, -6.4, -7.3, -8.8}),
        new FaixaPercentil(80, 84, new double[]{4.3, 2.8, 1.8, 0.9, 0.2, -0.4, -1.0, -1.6, -2.1, -2.6, -3.1, -3.7, -4.2, -4.8, -5.4, -6.1, -7.0, -8.0, -9.5}),
        new FaixaPercentil(85, 89, new double[]{3.5, 1.9, 0.8, -0.1, -0.9, -1.6, -2.1, -2.8, -3.3, -3.9, -4.5, -5.0, -5.7, -6.2, -6.9, -7.7, -8.6, -9.7, -11.3}),
        new FaixaPercentil(90, 94, new double[]{3.9, 2.2, 0.9, -0.1, -1.0, -1.8, -2.5, -3.2, -3.8, -4.5, -5.2, -5.8, -6.5, -7.2, -8.0, -8.9, -9.9, -11.2, -13.0}),
    };

    private static final FaixaPercentil[] FULLERTON_LEVANTAR_CAMINHAR_MASC_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{3.0, 3.0, 3.3, 3.6, 3.8, 4.0, 4.2, 4.4, 4.5, 4.7, 4.9, 5.0, 5.2, 5.4, 5.6, 5.8, 6.1, 6.4, 6.8}),
        new FaixaPercentil(65, 69, new double[]{3.1, 3.6, 3.9, 4.1, 4.3, 4.5, 4.6, 4.8, 4.9, 5.1, 5.3, 5.4, 5.6, 5.7, 5.9, 6.1, 6.3, 6.6, 7.1}),
        new FaixaPercentil(70, 74, new double[]{3.2, 3.6, 3.9, 4.2, 4.4, 4.6, 4.8, 5.0, 5.1, 5.3, 5.5, 5.6, 5.8, 6.0, 6.2, 6.4, 6.7, 7.0, 7.4}),
        new FaixaPercentil(75, 79, new double[]{3.3, 3.5, 3.9, 4.3, 4.6, 4.9, 5.2, 5.4, 5.7, 5.9, 6.1, 6.4, 6.6, 6.9, 7.2, 7.5, 7.9, 8.3, 9.0}),
        new FaixaPercentil(80, 84, new double[]{4.0, 4.1, 4.5, 4.9, 5.2, 5.5, 5.7, 6.0, 6.2, 6.4, 6.6, 6.9, 7.1, 7.3, 7.6, 7.9, 8.3, 8.7, 9.4}),
        new FaixaPercentil(85, 89, new double[]{4.0, 4.3, 4.5, 5.0, 5.5, 5.8, 6.2, 6.5, 6.9, 7.2, 7.5, 7.9, 8.2, 8.6, 8.9, 9.47, 9.9, 10.5, 11.5}),
        new FaixaPercentil(90, 94, new double[]{4.3, 4.5, 5.1, 5.7, 6.2, 6.6, 7.0, 7.4, 7.7, 8.1, 8.5, 8.8, 9.2, 9.6, 10.0, 10.5, 11.1, 11.8, 12.9}),
    };
    private static final FaixaPercentil[] FULLERTON_LEVANTAR_CAMINHAR_FEM_PERCENTIL = {
        new FaixaPercentil(60, 64, new double[]{3.2, 3.7, 4.0, 4.2, 4.4, 4.6, 4.7, 4.9, 5.0, 5.2, 5.4, 5.5, 5.7, 5.8, 6.0, 6.2, 6.4, 6.7, 7.2}),
        new FaixaPercentil(65, 69, new double[]{3.6, 4.1, 4.4, 4.6, 4.8, 5.0, 5.1, 5.3, 5.4, 5.6, 5.8, 5.9, 6.1, 6.2, 6.4, 6.6, 6.8, 7.1, 7.6}),
        new FaixaPercentil(70, 74, new double[]{3.8, 4.0, 4.3, 4.7, 4.9, 5.2, 5.4, 5.6, 5.8, 6.0, 6.2, 6.4, 6.6, 6.8, 7.1, 7.3, 7.7, 8.0, 8.6}),
        new FaixaPercentil(75, 79, new double[]{4.0, 4.3, 4.6, 5.0, 5.2, 5.5, 5.7, 5.9, 6.1, 6.3, 6.5, 6.7, 6.9, 7.1, 7.4, 7.6, 8.0, 8.3, 8.9}),
        new FaixaPercentil(80, 84, new double[]{4.0, 4.4, 4.9, 5.4, 5.7, 6.1, 6.3, 6.7, 6.9, 7.2, 7.2, 7.8, 8.1, 8.3, 8.7, 9.0, 9.5, 10.0, 10.8}),
        new FaixaPercentil(85, 89, new double[]{4.5, 4.7, 5.3, 5.8, 6.2, 6.6, 6.9, 7.3, 7.6, 7.9, 8.2, 8.5, 8.9, 9.2, 9.6, 10.0, 10.5, 11.1, 12.0}),
        new FaixaPercentil(90, 94, new double[]{5.0, 5.3, 6.1, 6.7, 7.3, 7.7, 8.2, 8.6, 9.0, 9.4, 9.8, 10.2, 10.6, 11.1, 11.5, 12.1, 12.7, 13.5, 14.6}),
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
        criar(properties.getEsteiraIncrementalId(), "Classificação VO₂ Máx - Esteira Incremental (ACSM/AHA)", this::criarRaizEsteiraIncremental);
        criar(properties.getImcId(), "Classificação IMC - OMS", this::criarRaizImc);
        criarFullerton();
    }

    private void criarFullerton() {
        salvarOuAtualizar(properties.getFullertonSentarLevantarId(), "Classificação Fullerton - Sentar e Levantar 30s",
                this::criarRaizFullertonSentarLevantar);
        salvarOuAtualizar(properties.getFullertonFlexaoCotoveloId(), "Classificação Fullerton - Flexão de Cotovelo 30s",
                this::criarRaizFullertonFlexaoCotovelo);
        salvarOuAtualizar(properties.getFullertonMarcha2MinId(), "Classificação Fullerton - Marcha Estacionária 2min",
                this::criarRaizFullertonMarcha2Min);
        salvarOuAtualizar(properties.getFullertonSentarAlcancarPesId(), "Classificação Fullerton - Sentar e Alcançar os Pés",
                this::criarRaizFullertonSentarAlcancarPes);
        salvarOuAtualizar(properties.getFullertonAlcancarCostasId(), "Classificação Fullerton - Alcançar as Costas",
                this::criarRaizFullertonAlcancarCostas);
        salvarOuAtualizar(properties.getFullertonLevantarCaminharId(), "Classificação Fullerton - Levantar e Caminhar 2,5m",
                this::criarRaizFullertonLevantarCaminhar);
    }

    private PersistedTabelaClassificacaoGenerica criarRaizFullertonSentarLevantar() {
        var raiz = new PersistedTabelaClassificacaoGenerica();

        var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
        for (var fp : FULLERTON_SENTAR_LEVANTAR_MASC_PERCENTIL) masc.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(masc);

        var fem = new PersistedTabelaSexo(Sexo.FEMININO);
        for (var fp : FULLERTON_SENTAR_LEVANTAR_FEM_PERCENTIL) fem.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(fem);

        return raiz;
    }

    private PersistedTabelaClassificacaoGenerica criarRaizFullertonFlexaoCotovelo() {
        var raiz = new PersistedTabelaClassificacaoGenerica();

        var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
        for (var fp : FULLERTON_FLEXAO_MASC_PERCENTIL) masc.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(masc);

        var fem = new PersistedTabelaSexo(Sexo.FEMININO);
        for (var fp : FULLERTON_FLEXAO_FEM_PERCENTIL) fem.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(fem);

        return raiz;
    }

    private PersistedTabelaClassificacaoGenerica criarRaizFullertonMarcha2Min() {
        var raiz = new PersistedTabelaClassificacaoGenerica();

        var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
        for (var fp : FULLERTON_MARCHA_MASC_PERCENTIL) masc.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(masc);

        var fem = new PersistedTabelaSexo(Sexo.FEMININO);
        for (var fp : FULLERTON_MARCHA_FEM_PERCENTIL) fem.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(fem);

        return raiz;
    }

    private PersistedTabelaClassificacaoGenerica criarRaizFullertonSentarAlcancarPes() {
        var raiz = new PersistedTabelaClassificacaoGenerica();

        var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
        for (var fp : FULLERTON_SENTAR_ALCANCAR_MASC_PERCENTIL) masc.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(masc);

        var fem = new PersistedTabelaSexo(Sexo.FEMININO);
        for (var fp : FULLERTON_SENTAR_ALCANCAR_FEM_PERCENTIL) fem.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(fem);

        return raiz;
    }

    private PersistedTabelaClassificacaoGenerica criarRaizFullertonAlcancarCostas() {
        var raiz = new PersistedTabelaClassificacaoGenerica();

        var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
        for (var fp : FULLERTON_ALCANCAR_COSTAS_MASC_PERCENTIL) masc.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(masc);

        var fem = new PersistedTabelaSexo(Sexo.FEMININO);
        for (var fp : FULLERTON_ALCANCAR_COSTAS_FEM_PERCENTIL) fem.addComponente(criarFaixaPercentil(fp));
        raiz.addComponente(fem);

        return raiz;
    }

    private PersistedTabelaClassificacaoGenerica criarRaizFullertonLevantarCaminhar() {
        var raiz = new PersistedTabelaClassificacaoGenerica();

        var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
        for (var fp : FULLERTON_LEVANTAR_CAMINHAR_MASC_PERCENTIL) masc.addComponente(criarFaixaPercentilTempo(fp));
        raiz.addComponente(masc);

        var fem = new PersistedTabelaSexo(Sexo.FEMININO);
        for (var fp : FULLERTON_LEVANTAR_CAMINHAR_FEM_PERCENTIL) fem.addComponente(criarFaixaPercentilTempo(fp));
        raiz.addComponente(fem);

        return raiz;
    }

    private static PersistedTabelaIdade criarFaixaPercentilTempo(FaixaPercentil fp) {
        var idade = new PersistedTabelaIdade(fp.idadeMin(), fp.idadeMax());
        Double minAnterior = null;
        for (int i = 0; i < PERCENTIS_FULLERTON.length; i++) {
            int p = PERCENTIS_FULLERTON[i];
            double corte = fp.cortes()[i];
            if (minAnterior != null && corte < minAnterior) {
                corte = minAnterior;
            }
            if (i == 0) {
                idade.addComponente(new PersistedPercentilFuncional(
                    p, null, corte, null, TipoLimite.INCLUSIVO
                ));
                minAnterior = corte;
            } else {
                Double min = minAnterior;
                TipoLimite tipoMin = (min != null && corte > min) ? TipoLimite.EXCLUSIVO : TipoLimite.INCLUSIVO;
                idade.addComponente(new PersistedPercentilFuncional(
                    p, min, corte, tipoMin, TipoLimite.INCLUSIVO
                ));
                if (corte > minAnterior) {
                    minAnterior = corte;
                }
            }
        }
        double corteP5 = fp.cortes()[PERCENTIS_FULLERTON.length - 1];
        if (minAnterior != null && corteP5 < minAnterior) {
            corteP5 = minAnterior;
        }
        idade.addComponente(new PersistedPercentilFuncional(
            null, corteP5, null, TipoLimite.EXCLUSIVO, null
        ));
        return idade;
    }

    private static PersistedTabelaIdade criarFaixaPercentil(FaixaPercentil fp) {
        var idade = new PersistedTabelaIdade(fp.idadeMin(), fp.idadeMax());
        Double maxAnterior = null;
        for (int i = 0; i < PERCENTIS_FULLERTON.length; i++) {
            int p = PERCENTIS_FULLERTON[i];
            double corte = fp.cortes()[i];
            if (maxAnterior != null && corte > maxAnterior) {
                corte = maxAnterior;
            }
            if (i == 0) {
                idade.addComponente(new PersistedPercentilFuncional(
                    p, corte, null, TipoLimite.INCLUSIVO, null
                ));
                maxAnterior = corte;
            } else {
                Double max = maxAnterior;
                TipoLimite tipoMax = (max != null && max > corte) ? TipoLimite.EXCLUSIVO : TipoLimite.INCLUSIVO;
                idade.addComponente(new PersistedPercentilFuncional(
                    p, corte, max, TipoLimite.INCLUSIVO, tipoMax
                ));
                if (corte < maxAnterior) {
                    maxAnterior = corte;
                }
            }
        }
        double corteP5 = fp.cortes()[PERCENTIS_FULLERTON.length - 1];
        if (maxAnterior != null && corteP5 > maxAnterior) {
            corteP5 = maxAnterior;
        }
        idade.addComponente(new PersistedPercentilFuncional(
            null, null, corteP5, null, TipoLimite.EXCLUSIVO
        ));
        return idade;
    }

    private PersistedTabelaClassificacaoGenerica criarRaizFullerton(Faixa[] masc, Faixa[] fem) {
        var raiz = new PersistedTabelaClassificacaoGenerica();
        preencherSexosGenerico(raiz, masc, fem);
        return raiz;
    }

    private void salvarOuAtualizar(String id, String descricao, Supplier<? extends PersistedComposite> raizSupplier) {
        var doc = repository.findById(id).orElseGet(() -> new TabelaClassificacaoDocument(id, descricao));
        doc.setNome(descricao);
        doc.setRaiz(raizSupplier.get());
        repository.save(doc);
        log.info("Tabela '{}' atualizada/salva: {}", descricao, id);
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
        if (masculino != null) {
            var masc = new PersistedTabelaSexo(Sexo.MASCULINO);
            for (var f : masculino) masc.addComponente(criarFaixa(f));
            raiz.addComponente(masc);
        }

        if (feminino != null) {
            var fem = new PersistedTabelaSexo(Sexo.FEMININO);
            for (var f : feminino) fem.addComponente(criarFaixa(f));
            raiz.addComponente(fem);
        }
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
