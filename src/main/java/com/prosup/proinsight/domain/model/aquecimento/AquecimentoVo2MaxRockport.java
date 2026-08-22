package com.prosup.proinsight.domain.model.aquecimento;

import com.prosup.proinsight.domain.enums.Protocolo;

/**
 * Aquecimento para teste Rockport (1 milha).
 * Registra tempo e distância percorrida no aquecimento.
 * NÃO calcula VO2Max — apenas registra dados.
 */
public class AquecimentoVo2MaxRockport extends AquecimentoVo2Max {

    private Integer distanciaMetros;

    public AquecimentoVo2MaxRockport() {
        super(Protocolo.ROCKPORT);
    }

    public AquecimentoVo2MaxRockport(Double tempoMinutos, Integer distanciaMetros) {
        super(Protocolo.ROCKPORT);
        this.tempoMinutos = tempoMinutos;
        this.distanciaMetros = distanciaMetros;
    }

    @Override
    public String gerarCodigo() {
        return "AQUEC_ROCKPORT_" + System.currentTimeMillis();
    }

    @Override
    public String getDescricao() {
        if (tempoMinutos == null && distanciaMetros == null) {
            return "Aquecimento Rockport (sem dados)";
        }
        StringBuilder sb = new StringBuilder("Aquecimento Rockport: ");
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
}
