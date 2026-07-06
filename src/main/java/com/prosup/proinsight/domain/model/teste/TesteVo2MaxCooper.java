package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

public class TesteVo2MaxCooper extends TesteVo2Max {

    private Integer distanciaMetros;

    public TesteVo2MaxCooper() {
    }

    public TesteVo2MaxCooper(Integer distanciaMetros) {
        super(ProtocoloVo2Max.COOPER, distanciaMetros != null ? distanciaMetros.doubleValue() : null);
        this.distanciaMetros = distanciaMetros;
    }

    @Override
    public String gerarCodigo() {
        return "COOPER_" + System.currentTimeMillis();
    }

    @Override
    public String getValorClassificacao() {
        return distanciaMetros != null ? String.valueOf(distanciaMetros.doubleValue()) : null;
    }

    public Integer getDistanciaMetros() {
        return distanciaMetros;
    }

    public void setDistanciaMetros(Integer distanciaMetros) {
        this.distanciaMetros = distanciaMetros;
        this.valorClassificacao = distanciaMetros != null ? distanciaMetros.doubleValue() : null;
    }

    public Double getDistanciaKm() {
        return distanciaMetros != null ? distanciaMetros / 1000.0 : null;
    }
}
