package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.model.composite.Component;

public class TabelaClassificacao {

    private String id;
    private String nome;
    private Component raiz;

    public TabelaClassificacao() {
    }

    public TabelaClassificacao(String id, String nome, Component raiz) {
        this.id = id;
        this.nome = nome;
        this.raiz = raiz;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Component getRaiz() {
        return raiz;
    }
}
