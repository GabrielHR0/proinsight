package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.Protocolo;

public class TesteVo2MaxEsteiraIncremental extends TesteVo2Max {

    private Double velocidadeKmh;
    private Double inclinacaoPercent;

    public TesteVo2MaxEsteiraIncremental() {}

    public TesteVo2MaxEsteiraIncremental(Double velocidadeKmh) {
        this(velocidadeKmh, 0.0);
    }

    public TesteVo2MaxEsteiraIncremental(Double velocidadeKmh, Double inclinacaoPercent) {
        super(Protocolo.ESTEIRA_INCREMENTAL, null);
        this.velocidadeKmh = velocidadeKmh;
        this.inclinacaoPercent = inclinacaoPercent != null ? inclinacaoPercent : 0.0;
    }

    @Override
    public Double calcularVo2Max(DadosAvaliacao dados) {
        if (velocidadeKmh == null) return null;
        double velocidadeMmin = velocidadeKmh * 16.67;

        if (velocidadeKmh <= 6.0) {
            return (0.1 * velocidadeMmin) + 3.5;
        }
        return (0.2 * velocidadeMmin) + 3.5;
    }

    @Override
    public String gerarCodigo() {
        return "ESTEIRA_INCREMENTAL_" + System.currentTimeMillis();
    }

    @Override
    public String getValorClassificacao() {
        if (velocidadeKmh == null) return null;
        double velocidadeMmin = velocidadeKmh * 16.67;
        double vo2max;
        if (velocidadeKmh <= 6.0) {
            vo2max = (0.1 * velocidadeMmin) + 3.5;
        } else {
            vo2max = (0.2 * velocidadeMmin) + 3.5;
        }
        return String.valueOf(vo2max);
    }

    @Override
    public String getValorClassificacao(DadosAvaliacao dados) {
        Double vo2max = calcularVo2Max(dados);
        if (vo2max != null) {
            this.valorClassificacao = vo2max;
        }
        return vo2max != null ? String.valueOf(vo2max) : null;
    }

    public Double getVelocidadeKmh() {
        return velocidadeKmh;
    }

    public void setVelocidadeKmh(Double velocidadeKmh) {
        this.velocidadeKmh = velocidadeKmh;
    }

    public Double getInclinacaoPercent() {
        return inclinacaoPercent;
    }

    public void setInclinacaoPercent(Double inclinacaoPercent) {
        this.inclinacaoPercent = inclinacaoPercent;
    }
}
