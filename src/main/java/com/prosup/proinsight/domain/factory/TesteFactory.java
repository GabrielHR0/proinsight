package com.prosup.proinsight.domain.factory;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;

public class TesteFactory {

    public TesteVo2MaxCooper TesteVo2MaxCooper(
            ProtocoloVo2Max protocolo
    ){
        if (!protocolo.equals(ProtocoloVo2Max.COOPER)){
            throw new IllegalArgumentException("Protocolo inválido para teste de VO2Max Cooper");
        } else {
            var teste = new TesteVo2MaxCooper();
            teste.setProtocolo(protocolo);
            return teste;
        }
    }
}
