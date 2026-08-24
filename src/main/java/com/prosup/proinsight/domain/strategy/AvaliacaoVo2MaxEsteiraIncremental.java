package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxEsteiraIncremental;
import org.springframework.stereotype.Component;

/**
 * Strategy específica para avaliação de VO2Max em esteira incremental adaptada.
 *
 * Fórmula ACSM/AHA adaptada (inclinação = 0%):
 *   Caminhada (≤6 km/h): VO₂ = (0.1 × velocidade em m/min) + 3,5
 *   Corrida  (>6 km/h):  VO₂ = (0.2 × velocidade em m/min) + 3,5
 *   Conversão: velocidade (m/min) = km/h × 16,67
 *   METs = VO₂ ÷ 3,5
 *
 * Exemplo: 10.5 km/h = 175.0 m/min (>6, então corrida)
 *   VO₂ = (0.2 × 175.0) + 3.5 = 38.5 mL/kg/min ≈ 11.0 METs
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
     * Calcula VO2Max para esteira incremental — fórmula ACSM/AHA adaptada.
     *
     * Caminhada (≤6 km/h): VO₂ = (0.1 × velocidade em m/min) + 3,5
     * Corrida  (>6 km/h):  VO₂ = (0.2 × velocidade em m/min) + 3,5
     * Conversão: velocidade (m/min) = km/h × 16,67
     *
     * @param velocidadeKmh Velocidade em km/h
     * @param inclinacaoPercent Inclinação em percentual (registrada, não entra no cálculo do adaptado)
     * @return VO2Max em mL/kg/min
     */
    public static Double calcularVo2Max(double velocidadeKmh, double inclinacaoPercent) {
        double velocidadeMmin = velocidadeKmh * 16.67;
        if (velocidadeKmh <= 6.0) {
            return (0.1 * velocidadeMmin) + 3.5;
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
