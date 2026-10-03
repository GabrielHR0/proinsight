package com.prosup.proinsight.domain.model;

import java.util.Map;

/**
 * Converte códigos técnicos de classificação (ex: "MUITO_RUIM", "OBESIDADE_I")
 * em textos legíveis por humanos (ex: "Muito ruim", "Obesidade I"),
 * preservando acentos para exibição ao usuário final.
 */
public final class ClassificacaoLegivel {

    private static final Map<String, String> LEGIVEL = Map.ofEntries(
        Map.entry("MUITO_RUIM", "Muito ruim"),
        Map.entry("RUIM", "Ruim"),
        Map.entry("MEDIO", "Médio"),
        Map.entry("MÉDIO", "Médio"),
        Map.entry("BOM", "Bom"),
        Map.entry("EXCELENTE", "Excelente"),
        Map.entry("BAIXO", "Baixo"),
        Map.entry("ELEVADO", "Elevado"),
        Map.entry("ABAIXO_DO_PESO", "Abaixo do peso"),
        Map.entry("NORMAL", "Normal"),
        Map.entry("SOBREPESO", "Sobrepeso"),
        Map.entry("OBESIDADE_I", "Obesidade I"),
        Map.entry("OBESIDADE_II", "Obesidade II"),
        Map.entry("OBESIDADE_III", "Obesidade III"),
        Map.entry("MENOR_QUE_P5", "Abaixo do percentil 5"),
        Map.entry("ABAIXO_PERCENTIL_5", "Abaixo do percentil 5")
    );

    private ClassificacaoLegivel() {}

    public static String humanizar(String classificacao) {
        if (classificacao == null) {
            return null;
        }
        if (LEGIVEL.containsKey(classificacao)) {
            return LEGIVEL.get(classificacao);
        }
        if (classificacao.startsWith("PERCENTIL_")) {
            return "Percentil " + classificacao.substring("PERCENTIL_".length());
        }
        return classificacao;
    }
}