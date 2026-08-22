package com.prosup.proinsight.infrastructure.persistence.document.composite;

import com.prosup.proinsight.domain.enums.Sexo;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedTabelaSexo")
public class PersistedTabelaSexo extends PersistedComposite {

    private Sexo sexo;

    public PersistedTabelaSexo() {
    }

    public PersistedTabelaSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
}
