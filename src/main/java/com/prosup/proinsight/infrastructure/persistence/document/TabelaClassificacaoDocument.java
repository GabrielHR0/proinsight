package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponent;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "tabelasClassificacao")
@TypeAlias("tabelaClassificacao")
public class TabelaClassificacaoDocument {

    @Id
    private String id;

    private String nome;

    private PersistedComponent raiz;

    public TabelaClassificacaoDocument() {
    }

    public TabelaClassificacaoDocument(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public PersistedComponent getRaiz() {
        return raiz;
    }

    public void setRaiz(PersistedComponent raiz) {
        this.raiz = raiz;
    }
}
