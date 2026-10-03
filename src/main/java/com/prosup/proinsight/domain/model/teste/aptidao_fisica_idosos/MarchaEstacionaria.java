package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;

public class MarchaEstacionaria extends TesteAptdaoFisicaIdosos {

    public Integer passos;

    public MarchaEstacionaria(TesteFuncionalTipo tipo, Double valor, Integer passos) {
        super(tipo, valor);
        this.passos = passos;
    }

    public MarchaEstacionaria(TesteFuncionalTipo tipo, Double valor) {
        super(tipo, valor);
        this.passos = valor != null ? valor.intValue() : null;
    }

    @Override
    public String gerarCodigo() {
        return "FUNME2M-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
