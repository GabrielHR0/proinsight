package com.prosup.proinsight.infrastructure.persistence.document.composite;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedTabelaVo2Max")
public class PersistedTabelaVo2Max extends PersistedComposite {

    private ProtocoloVo2Max protocolo;

    public PersistedTabelaVo2Max() {}

    public PersistedTabelaVo2Max(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }

    public ProtocoloVo2Max getProtocolo() {
        return this.protocolo;
    }

    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }
}
