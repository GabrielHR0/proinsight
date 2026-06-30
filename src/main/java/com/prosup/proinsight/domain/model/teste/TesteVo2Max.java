package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.enums.Unidade;

public abstract class TesteVo2Max implements Teste {

    private final String codigo =  gerarCodigo();

    protected ProtocoloVo2Max protocolo;

    public TesteVo2Max(ProtocoloVo2Max protocolo, double resultado) {
        this.protocolo = protocolo;
    }

    public TesteVo2Max() {};

    public ProtocoloVo2Max getProtocolo() {
        return protocolo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }
}
