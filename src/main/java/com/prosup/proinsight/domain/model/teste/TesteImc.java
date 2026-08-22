package com.prosup.proinsight.domain.model.teste;

import java.util.Locale;

public class TesteImc implements Teste{

    private final String codigo = this.gerarCodigo();

    private Integer massaCorporalGramas;

    private Integer alturaCentimetros;

    @Override
    public String gerarCodigo() {
        return "IMC"+ System.currentTimeMillis();
    }

    @Override
    public String getCriterio() {
        return "IMC";
    }

    public TesteImc() {
    }

    public TesteImc(Integer massaCorporalGramas, Integer alturaCentimetros) {
        this.massaCorporalGramas = massaCorporalGramas;
        this.alturaCentimetros = alturaCentimetros;
    }

    public String getCodigo() {
        return codigo;
    }

    @Override
    public String getValorClassificacao() {
        if (massaCorporalGramas == null || alturaCentimetros == null) return null;
        double alturaMetros = alturaCentimetros / 100.0;
        double imc = (massaCorporalGramas / 1000.0) / (alturaMetros * alturaMetros);
        return String.format(Locale.US, "%.2f", imc);
    }

    public Integer getMassaCorporalGramas() {
        return massaCorporalGramas;
    }

    public void setMassaCorporalGramas(Integer massaCorporalGramas) {
        this.massaCorporalGramas = massaCorporalGramas;
    }

    public Integer getAlturaCentimetros() {
        return alturaCentimetros;
    }

    public void setAlturaCentimetros(Integer alturaCentimetros) {
        this.alturaCentimetros = alturaCentimetros;
    }
}
