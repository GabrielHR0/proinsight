package com.prosup.proinsight.domain.model.aquecimento;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

/**
 * Aquecimento para teste de esteira incremental.
 * Registra velocidade, inclinação e tempo no aquecimento.
 * NÃO calcula VO2Max — apenas registra dados.
 */
public class AquecimentoVo2MaxEsteiraIncremental extends AquecimentoVo2Max {

    private Double velocidadeKmh;
    private Double inclinacaoPercent;

    public AquecimentoVo2MaxEsteiraIncremental() {
        super(ProtocoloVo2Max.ESTEIRA_INCREMENTAL);
    }

    public AquecimentoVo2MaxEsteiraIncremental(Double velocidadeKmh, Double inclinacaoPercent, Double tempoMinutos) {
        super(ProtocoloVo2Max.ESTEIRA_INCREMENTAL);
        this.velocidadeKmh = velocidadeKmh;
        this.inclinacaoPercent = inclinacaoPercent;
        this.tempoMinutos = tempoMinutos;
    }

    @Override
    public String gerarCodigo() {
        return "AQUEC_ESTEIRA_" + System.currentTimeMillis();
    }

    @Override
    public String getDescricao() {
        if (velocidadeKmh == null) return "Aquecimento Esteira (sem dados)";
        StringBuilder sb = new StringBuilder("Aquecimento Esteira: ");
        sb.append(velocidadeKmh).append(" km/h");
        if (inclinacaoPercent != null && inclinacaoPercent > 0) {
            sb.append(", inclinação ").append(inclinacaoPercent).append("%");
        }
        if (tempoMinutos != null) {
            sb.append(", ").append(tempoMinutos).append(" min");
        }
        return sb.toString();
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
