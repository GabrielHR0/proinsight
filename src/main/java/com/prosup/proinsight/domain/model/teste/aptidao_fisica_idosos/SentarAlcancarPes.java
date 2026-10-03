package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;

public class SentarAlcancarPes extends TesteAptdaoFisicaIdosos {

    public Double centimetros;

    public SentarAlcancarPes(TesteFuncionalTipo tipo, Double valor, Double centimetros) {
        super(tipo, valor);
        this.centimetros = centimetros;
    }

    public SentarAlcancarPes(TesteFuncionalTipo tipo, Double valor) {
        super(tipo, valor);
        this.centimetros = valor;
    }

    @Override
    public String gerarCodigo() {
        return "FUNSAP-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
