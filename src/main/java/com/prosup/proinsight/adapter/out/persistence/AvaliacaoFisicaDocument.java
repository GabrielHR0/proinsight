package com.prosup.proinsight.adapter.out.persistence;

import com.prosup.proinsight.domain.avalicao_strategy.AvaliacaoStrategy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "avaliacoesFisicas")
public class AvaliacaoFisicaDocument {

    @Id
    private String id;

    @Indexed
    private String clienteId;

    @Indexed
    private String avaliadorId;

    private AvaliacaoStrategy<?> strategy;
    private List<MedicaoDocument> medicoes = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public AvaliacaoFisicaDocument() {
    }

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

    public List<MedicaoDocument> getMedicoes() {
        return medicoes;
    }

    public void setMedicoes(List<MedicaoDocument> medicoes) {
        this.medicoes = medicoes == null ? new ArrayList<>() : new ArrayList<>(medicoes);
    }

    public AvaliacaoStrategy<?> getStrategy() {
        return strategy;
    }

    public void setStrategy(AvaliacaoStrategy<?> strategy) {
        this.strategy = strategy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
