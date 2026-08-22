package com.prosup.proinsight.domain.model.aquecimento;

import com.prosup.proinsight.domain.enums.Protocolo;

/**
 * Aquecimento para teste Cooper (12 minutos).
 * Registra distância percorrida e tempo no aquecimento.
 * NÃO calcula VO2Max — apenas registra dados.
 */
public class AquecimentoVo2MaxCooper extends AquecimentoVo2Max {

    private Integer distanciaMetros;

    public AquecimentoVo2MaxCooper() {
        super(Protocolo.COOPER);
    }

    public AquecimentoVo2MaxCooper(Integer distanciaMetros) {
        super(Protocolo.COOPER);
        this.distanciaMetros = distanciaMetros;
    }

    public AquecimentoVo2MaxCooper(Integer distanciaMetros, Double tempoMinutos) {
        super(Protocolo.COOPER);
        this.distanciaMetros = distanciaMetros;
        this.tempoMinutos = tempoMinutos;
    }

    @Override
    public String gerarCodigo() {
        return "AQUEC_COOPER_" + System.currentTimeMillis();
    }

    @Override
    public String getDescricao() {
        if (distanciaMetros == null && tempoMinutos == null) {
            return "Aquecimento Cooper (sem dados)";
        }
        StringBuilder sb = new StringBuilder("Aquecimento Cooper: ");
        if (distanciaMetros != null) {
            sb.append(distanciaMetros).append("m");
        }
        if (tempoMinutos != null) {
            if (distanciaMetros != null) sb.append(" em ");
            sb.append(tempoMinutos).append(" min");
        }
        return sb.toString();
    }

    public Integer getDistanciaMetros() {
        return distanciaMetros;
    }

    public void setDistanciaMetros(Integer distanciaMetros) {
        this.distanciaMetros = distanciaMetros;
    }

    public Double getDistanciaKm() {
        return distanciaMetros != null ? distanciaMetros / 1000.0 : null;
    }
}
