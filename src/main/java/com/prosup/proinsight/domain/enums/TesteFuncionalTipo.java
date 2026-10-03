package com.prosup.proinsight.domain.enums;

import java.util.Arrays;
import java.util.Locale;

public enum TesteFuncionalTipo {

    SENTAR_LEVANTAR_30S("Sentar e levantar da cadeira em 30 segundos", "repetições", "REGRESSIVA", 30, 0, 50),
    FLEXAO_COTOVELO_30S("Flexão de cotovelo em 30 segundos", "repetições", "REGRESSIVA", 30, 0, 50),
    MARCHA_ESTACIONARIA_2MIN("Marcha estacionária de 2 minutos", "passos", "REGRESSIVA", 120, 0, 300),
    SENTAR_ALCANCAR_PES("Sentar e alcançar os pés", "cm", "NENHUM", null, -30, 50),
    ALCANCAR_COSTAS("Alcançar as costas", "cm", "NENHUM", null, -30, 50),
    LEVANTAR_CAMINHAR_2M5("Levantar e caminhar 2,5 metros", "segundos", "CONTAGEM", null, 1, 120);

    private final String nome;
    private final String unidade;
    private final String timer;
    private final Integer segundos;
    private final double valorMin;
    private final double valorMax;

    TesteFuncionalTipo(String nome, String unidade, String timer, Integer segundos, double valorMin, double valorMax) {
        this.nome = nome;
        this.unidade = unidade;
        this.timer = timer;
        this.segundos = segundos;
        this.valorMin = valorMin;
        this.valorMax = valorMax;
    }

    public double getValorMin() {
        return valorMin;
    }

    public double getValorMax() {
        return valorMax;
    }

    public String getNome() {
        return nome;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getTimer() {
        return timer;
    }

    public Integer getSegundos() {
        return segundos;
    }

    public static TesteFuncionalTipo deChave(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("teste é obrigatório");
        }
        try {
            return valueOf(chave.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Teste funcional não suportado: " + chave + ". Valores aceitos: " + Arrays.toString(values())
            );
        }
    }
}
