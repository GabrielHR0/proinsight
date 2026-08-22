package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxEsteiraIncremental;
import org.springframework.stereotype.Component;

/**
 * Strategy específica para avaliação de VO2Max em esteira incremental adaptada.
 *
 * Fórmula ACSM para corrida em esteira:
 *   VO₂ (mL·kg⁻¹·min⁻¹) = (0.2 × velocidade em m/min) + (0.9 × velocidade em m/min × inclinação/100) + 3.5
 *
 * Quando inclinação = 0%:
 *   VO₂ = (0.2 × velocidade) + 3.5
 *
 * Exemplo: 14.3 km/h = 238.3 m/min
 *   VO₂ = (0.2 × 238.3) + 3.5 ≈ 51.2 mL/kg/min ≈ 14.6 METs
 */
@Component
@StrategyFor("VO2_MAX_ESTEIRA_INCREMENTAL")
public class AvaliacaoVo2MaxEsteiraIncremental implements AvaliacaoStrategy<AvaliacaoVo2MaxContext> {

    public AvaliacaoVo2MaxEsteiraIncremental() {}

    @Override
    public Leaf avaliar(AvaliacaoVo2MaxContext contexto) {
        var tabelaClassificacao = contexto.getTabela();
        var dados = contexto.getDadosAvaliacao();

        for (TesteVo2Max teste : contexto.getTestes()) {
            var resultado = tabelaClassificacao.classificarComTeste(teste, dados);

            if (resultado instanceof NivelVo2Max n) {
                Double vo2 = teste.calcularVo2Max(dados);
                n.setResultadoVo2Max(vo2 != null ? (int) Math.round(vo2) : null);
                return n;
            }

            if (resultado != null) {
                return resultado;
            }
        }

        return null;
    }

    /**
     * Calcula VO2Max para esteira incremental com inclinação.
     *
     * @param velocidadeKmh Velocidade em km/h
     * @param inclinacaoPercent Inclinação em percentual (0-15%)
     * @return VO2Max em mL/kg/min
     */
    public static Double calcularVo2Max(double velocidadeKmh, double inclinacaoPercent) {
        double velocidadeMmin = velocidadeKmh * 1000.0 / 60.0;
        if (inclinacaoPercent > 0) {
            return (0.2 * velocidadeMmin) + (0.9 * velocidadeMmin * inclinacaoPercent / 100.0) + 3.5;
        }
        return (0.2 * velocidadeMmin) + 3.5;
    }

    /**
     * Calcula METs a partir do VO2Max.
     *
     * @param vo2max VO2Max em mL/kg/min
     * @return METs
     */
    public static Double calcularMets(double vo2max) {
        return vo2max / 3.5;
    }
}
