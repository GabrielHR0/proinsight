package com.prosup.proinsight.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

public class TesteVo2MaxDto {

    @JsonProperty("protocolo")
    private ProtocoloVo2Max protocolo;

    @JsonProperty("resultado")
    private double resultado;

    public TesteVo2MaxDto() {}

    public TesteVo2MaxDto(ProtocoloVo2Max protocolo, double
            resultado) {
        this.protocolo = protocolo;
        this.resultado = resultado;
    }

    public ProtocoloVo2Max getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }

    public double getResultado() {
        return resultado;
    }

    public void setResultado(double resultado) {
        this.resultado = resultado;
    }
}
