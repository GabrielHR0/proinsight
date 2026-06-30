package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;

import java.time.Instant;
import java.util.List;


public class MedicaoVo2 extends Medicao<TesteVo2Max> {

    public MedicaoVo2() {
        super();
    }

    public MedicaoVo2(Instant medidoEm, Instant createdAt, Instant updatedAt, String observacoes, String tabelaClassificacaoId, List<TesteVo2Max> testes) {
        super(MedicaoTipo.VO2_MAX, medidoEm, createdAt, updatedAt, observacoes, tabelaClassificacaoId, testes);
    }

    public MedicaoVo2(List<TesteVo2Max> testes) {
        this();
        this.setTestes(testes);
    }
}

