package com.prosup.proinsight.domain.model.teste;

public class TesteImc implements Teste {

    private final String codigo = this.gerarCodigo();

    private Integer massaCorporalGramas;

    private Integer alturaCentimetros;

    @Override
    public String gerarCodigo() {
        return "IMC-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
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
    public Double getValorNumerico() {
        if (massaCorporalGramas == null || alturaCentimetros == null || alturaCentimetros == 0) return null;
        double alturaMetros = alturaCentimetros / 100.0;
        return (massaCorporalGramas / 1000.0) / (alturaMetros * alturaMetros);
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
