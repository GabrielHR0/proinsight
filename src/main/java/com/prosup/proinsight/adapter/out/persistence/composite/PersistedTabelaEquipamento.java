package com.prosup.proinsight.adapter.out.persistence.composite;

import com.prosup.proinsight.domain.enums.Equipamento;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedTabelaEquipamento")
public class PersistedTabelaEquipamento extends PersistedComposite {

    private Equipamento equipamento;

    public PersistedTabelaEquipamento() {
    }

    public PersistedTabelaEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }
}
