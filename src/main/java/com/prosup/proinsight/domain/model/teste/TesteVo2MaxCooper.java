package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

public class TesteVo2MaxCooper extends TesteVo2Max {

    private Double distanciaMetros;


    public TesteVo2MaxCooper() {
    }

    public TesteVo2MaxCooper(Double distanciaMetros) {
        this();
        this.distanciaMetros = distanciaMetros;
        this.protocolo = ProtocoloVo2Max.COOPER;
    }

    @Override
    public String gerarCodigo() {
        return "COOPER_" + System.currentTimeMillis();
    }

    public Double getDistanciaMetros() {
        return distanciaMetros;
    }

    public void setDistanciaMetros(Double distanciaMetros) {
        this.distanciaMetros = distanciaMetros;
    }
}
