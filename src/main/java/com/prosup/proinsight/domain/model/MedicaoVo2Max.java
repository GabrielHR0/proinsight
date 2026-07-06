package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;

import java.time.Instant;
import java.util.List;


public class MedicaoVo2Max extends Medicao<TesteVo2Max> {

    private Integer resultado;

    public MedicaoVo2Max() {
    }

    public MedicaoVo2Max(MedicaoTipo tipo) {
        super(tipo);
    }

    public MedicaoVo2Max(MedicaoTipo tipo, Instant medidoEm, Instant createdAt, Instant updatedAt, String observacoes, String tabelaClassificacaoId, List<TesteVo2Max> testes) {
        super(tipo, medidoEm, createdAt, updatedAt, observacoes, tabelaClassificacaoId, testes);
    }

    public Integer getResultado() {
        return resultado;
    }

    public void setResultado(Integer resultado) {
        this.resultado = resultado;
    }

}
