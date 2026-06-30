package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

public class TabelaVo2Max extends Composite {

    private ProtocoloVo2Max protocolo;

    public ProtocoloVo2Max getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }
    
    @Override
    public Leaf classificarComTeste(Teste teste) {
        if (protocolo == null || !teste.getCriterio().equals(protocolo.name())) {
            return null;
        }
        
        for (Component child : getChildren()) {
            Leaf result = child.classificarComTeste(teste);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
}
