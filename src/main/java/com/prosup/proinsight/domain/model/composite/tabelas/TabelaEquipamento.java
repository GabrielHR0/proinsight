package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.enums.Equipamento;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

public class TabelaEquipamento extends Composite {

    private Equipamento equipamento;

    public TabelaEquipamento() {}

    public TabelaEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }
}
