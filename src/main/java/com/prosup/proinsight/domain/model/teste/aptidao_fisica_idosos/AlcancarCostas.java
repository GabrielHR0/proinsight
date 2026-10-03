package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;

public class AlcancarCostas extends TesteAptdaoFisicaIdosos {

    public Double centimetros;

    public AlcancarCostas(TesteFuncionalTipo tipo, Double valor, Double centimetros) {
        super(tipo, valor);
        this.centimetros = centimetros;
    }

    public AlcancarCostas(TesteFuncionalTipo tipo, Double valor) {
        super(tipo, valor);
        this.centimetros = valor;
    }

    @Override
    public String gerarCodigo() {
        return "FUNAC-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
