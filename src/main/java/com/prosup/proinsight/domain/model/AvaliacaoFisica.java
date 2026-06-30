package com.prosup.proinsight.domain.model;

import java.util.List;
import java.util.Map;

public class AvaliacaoFisica {

    private String id;
    private String clienteId;
    private String avaliadorId;
    private String strategyKey;
    private String versaoProtocolo;
    private List<Medicao> medicoes;
    private Map<String, Object> metadados;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getAvaliadorId() {
        return avaliadorId;
    }

    public void setAvaliadorId(String avaliadorId) {
        this.avaliadorId = avaliadorId;
    }

    public String getStrategyKey() {
        return strategyKey;
    }

    public void setStrategyKey(String strategyKey) {
        this.strategyKey = strategyKey;
    }

    public String getVersaoProtocolo() {
        return versaoProtocolo;
    }

    public void setVersaoProtocolo(String versaoProtocolo) {
        this.versaoProtocolo = versaoProtocolo;
    }

    public List<Medicao> getMedicoes() {
        return medicoes;
    }

    public void setMedicoes(List<Medicao> medicoes) {
        this.medicoes = medicoes;
    }

    public Map<String, Object> getMetadados() {
        return metadados;
    }

    public void setMetadados(Map<String, Object> metadados) {
        this.metadados = metadados;
    }
}
