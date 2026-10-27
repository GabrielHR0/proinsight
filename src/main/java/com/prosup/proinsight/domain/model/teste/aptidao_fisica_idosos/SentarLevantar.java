package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;

public class SentarLevantar extends TesteAptdaoFisicaIdosos {

    private Integer repeticoes;

    public Integer getRepeticoes() {
        return repeticoes;
    }

    public void setRepeticoes(Integer repeticoes) {
        this.repeticoes = repeticoes;
    }

    public SentarLevantar(TesteFuncionalTipo tipo, Double valor, Integer repeticoes) {
        super(tipo, valor);
        this.repeticoes = repeticoes;
    }

    public SentarLevantar(TesteFuncionalTipo tipo, Double valor) {
        super(tipo, valor);
        this.repeticoes = valor != null ? valor.intValue() : null;
    }

    @Override
    public String gerarCodigo() {
        return "FUNCSL30S-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
