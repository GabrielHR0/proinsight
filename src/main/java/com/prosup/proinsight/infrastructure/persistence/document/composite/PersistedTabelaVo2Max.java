package com.prosup.proinsight.infrastructure.persistence.document.composite;

import com.prosup.proinsight.domain.enums.Protocolo;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedTabelaVo2Max")
public class PersistedTabelaVo2Max extends PersistedComposite {

    private Protocolo protocolo;

    public PersistedTabelaVo2Max() {}

    public PersistedTabelaVo2Max(Protocolo protocolo) {
        this.protocolo = protocolo;
    }

    public Protocolo getProtocolo() {
        return this.protocolo;
    }

    public void setProtocolo(Protocolo protocolo) {
        this.protocolo = protocolo;
    }
}
