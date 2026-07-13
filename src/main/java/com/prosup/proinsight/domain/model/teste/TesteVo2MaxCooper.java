package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;
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
    public Double calcularVo2Max(DadosAvaliacao dados) {
        if (distanciaMetros == null) return null;
        return (distanciaMetros - 504.9) / 44.73;
    }

    @Override
    public String gerarCodigo() {
        return "COOPER_" + System.currentTimeMillis();
    }

    @Override
    public String getValorClassificacao() {
        return distanciaMetros != null ? String.valueOf(distanciaMetros.doubleValue()) : null;
    }

    @Override
    public String getValorClassificacao(DadosAvaliacao dados) {
        Double vo2max = calcularVo2Max(dados);
        return vo2max != null ? String.valueOf(vo2max) : null;
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
