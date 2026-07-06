package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.teste.TesteImc;

import java.time.Instant;
import java.util.List;

public class MedicaoImc extends Medicao<TesteImc>{

    private Integer resultado;

    public MedicaoImc() {
    }

    public MedicaoImc(MedicaoTipo tipo) {
        super(tipo);
    }

    public MedicaoImc(MedicaoTipo tipo, Instant medidoEm, Instant createdAt, Instant updatedAt, String observacoes, String tabelaClassificacaoId, List<TesteImc> testes) {
        super(tipo, medidoEm, createdAt, updatedAt, observacoes, tabelaClassificacaoId, testes);
    }

    public Integer getResultado() {
        return resultado;
    }

    public void setResultado(Integer resultado) {
        this.resultado = resultado;
    }

    @Override
    public void setTestes(List<TesteImc> testes) {

    }

    @Override
    public void addTestes(TesteImc teste) {
        this.getTestes().set(0, teste);
    }

    public TesteImc getTeste(){
        return this.getTestes().get(0);
    }
}
