package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

public class TesteVo2MaxEsteiraIncremental extends TesteVo2Max {

    private Double velocidadeKmh;

    public TesteVo2MaxEsteiraIncremental() {}

    public TesteVo2MaxEsteiraIncremental(Double velocidadeKmh) {
        super(ProtocoloVo2Max.ESTEIRA_INCREMENTAL, null);
        this.velocidadeKmh = velocidadeKmh;
    }

    @Override
    public Double calcularVo2Max(DadosAvaliacao dados) {
        if (velocidadeKmh == null) return null;
        double velocidadeMmin = velocidadeKmh * 1000.0 / 60.0;
        return (0.2 * velocidadeMmin) + 3.5;
    }

    @Override
    public String gerarCodigo() {
        return "ESTEIRA_INCREMENTAL_" + System.currentTimeMillis();
    }

    @Override
    public String getValorClassificacao(DadosAvaliacao dados) {
        Double vo2max = calcularVo2Max(dados);
        return vo2max != null ? String.valueOf(vo2max) : null;
    }

    public Double getVelocidadeKmh() {
        return velocidadeKmh;
    }

    public void setVelocidadeKmh(Double velocidadeKmh) {
        this.velocidadeKmh = velocidadeKmh;
    }
}
