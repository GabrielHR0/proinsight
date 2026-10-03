package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.TesteAptdaoFisicaIdosos;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicaoFuncional extends Medicao<TesteAptdaoFisicaIdosos> {

    private Map<String, String> classificacoes = new LinkedHashMap<>();
    private Map<String, Integer> percentis = new LinkedHashMap<>();

    public MedicaoFuncional() {
    }

    public MedicaoFuncional(MedicaoTipo tipo) {
        super(tipo);
    }

    public MedicaoFuncional(MedicaoTipo tipo, Instant medidoEm, Instant createdAt, Instant updatedAt,
                            String observacoes, List<TesteAptdaoFisicaIdosos> testes) {
        super(tipo, medidoEm, createdAt, updatedAt, observacoes, testes);
    }

    public Map<String, String> getClassificacoes() {
        return classificacoes;
    }

    public void setClassificacoes(Map<String, String> classificacoes) {
        this.classificacoes = classificacoes != null
            ? new LinkedHashMap<>(classificacoes)
            : new LinkedHashMap<>();
    }

    public Map<String, Integer> getPercentis() {
        return percentis;
    }

    public void setPercentis(Map<String, Integer> percentis) {
        this.percentis = percentis != null
            ? new LinkedHashMap<>(percentis)
            : new LinkedHashMap<>();
    }

    @Override
    public void setTestes(List<TesteAptdaoFisicaIdosos> testes) {
        super.setTestes(testes);
    }

    @Override
    public void addTestes(TesteAptdaoFisicaIdosos teste) {
        super.addTestes(teste);
    }
}
