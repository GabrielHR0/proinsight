package com.prosup.proinsight.domain.model;

public final class PercentilFaixa {

    public static final String BAIXO = "BAIXO";
    public static final String NORMAL = "NORMAL";
    public static final String ELEVADO = "ELEVADO";

    private PercentilFaixa() {}

    public static String classificar(Number percentil) {
        if (percentil == null) {
            return null;
        }
        double valor = percentil.doubleValue();
        if (valor < 25) {
            return BAIXO;
        }
        if (valor > 75) {
            return ELEVADO;
        }
        return NORMAL;
    }
}
