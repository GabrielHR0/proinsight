package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;

public class LevantarCaminhar2m5 extends TesteAptdaoFisicaIdosos {

    public Double segundos;

    public LevantarCaminhar2m5(TesteFuncionalTipo tipo, Double valor, Double segundos) {
        super(tipo, valor);
        this.segundos = segundos;
    }

    public LevantarCaminhar2m5(TesteFuncionalTipo tipo, Double valor) {
        super(tipo, valor);
        this.segundos = valor;
    }

    @Override
    public String gerarCodigo() {
        return "FUNLC2M5-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
