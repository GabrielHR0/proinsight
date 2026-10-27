package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.Protocolo;

public class TesteVo2MaxCooper extends TesteVo2Max {

    private Integer distanciaMetros;

    public TesteVo2MaxCooper() {
    }

    public TesteVo2MaxCooper(Integer distanciaMetros) {
        super(Protocolo.COOPER, distanciaMetros != null ? distanciaMetros.doubleValue() : null);
        this.distanciaMetros = distanciaMetros;
    }

    @Override
    public Double calcularVo2Max(DadosAvaliacao dados) {
        if (distanciaMetros == null) return null;
        return (distanciaMetros - 504.9) / 44.73;
    }

    @Override
    public String gerarCodigo() {
        return "COOPER-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    public Double getValorNumerico() {
        return distanciaMetros != null ? distanciaMetros.doubleValue() : valorClassificacao;
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
