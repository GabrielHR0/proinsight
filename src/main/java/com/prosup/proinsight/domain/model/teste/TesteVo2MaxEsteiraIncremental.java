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
        return "ESTEIRA-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    public Double getValorNumerico() {
        return calcularVo2Max(null);
    }

    @Override
    public Double getValorNumerico(DadosAvaliacao dados) {
        return calcularVo2Max(dados);
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
