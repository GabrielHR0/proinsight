package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.teste.TesteImc;

import java.time.Instant;
import java.util.List;

public class MedicaoImc extends Medicao<TesteImc>{

    private Double resultado;

    public MedicaoImc() {
    }

    public MedicaoImc(MedicaoTipo tipo) {
        super(tipo);
    }

    public MedicaoImc(MedicaoTipo tipo, Instant medidoEm, Instant createdAt, Instant updatedAt, String observacoes, List<TesteImc> testes) {
        super(tipo, medidoEm, createdAt, updatedAt, observacoes, testes);
    }

    public Double getResultado() {
        return resultado;
    }

    public void setResultado(Double resultado) {
        this.resultado = resultado;
    }

    @Override
    public void setTestes(List<TesteImc> testes) {
        super.setTestes(testes);
    }

    @Override
    public void addTestes(TesteImc teste) {
        super.addTestes(teste);
    }

    public TesteImc getTeste(){
        return this.getTestes().get(0);
    }
}
