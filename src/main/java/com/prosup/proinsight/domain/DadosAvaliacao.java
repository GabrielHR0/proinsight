package com.prosup.proinsight.domain;

import com.prosup.proinsight.domain.enums.Sexo;

import java.util.HashMap;
import java.util.Map;

public class DadosAvaliacao {

    private final Map<String, Object> dados = new HashMap<>();

    public DadosAvaliacao adicionar(String chave, Object valor) {
        dados.put(chave, valor);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String chave) {
        return (T) dados.get(chave);
    }

    public boolean tem(String chave) {
        return dados.containsKey(chave);
    }

    public Integer getIdade() {
        Object valor = dados.get("idade");
        return valor instanceof Integer ? (Integer) valor : null;
    }

    public Sexo getSexo() {
        Object valor = dados.get("sexo");
        return valor instanceof Sexo ? (Sexo) valor : null;
    }

    public boolean temIdade() {
        return dados.containsKey("idade") && dados.get("idade") != null;
    }

    public boolean temSexo() {
        return dados.containsKey("sexo") && dados.get("sexo") != null;
    }
}
