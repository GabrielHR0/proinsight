package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.config.ScopedByAcademia;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "avaliacoesFisicas")
@ScopedByAcademia
@CompoundIndexes({
        @CompoundIndex(name = "academia_cliente_idx", def = "{'academiaId': 1, 'clienteId': 1}"),
        @CompoundIndex(name = "academia_avaliador_idx", def = "{'academiaId': 1, 'avaliadorId': 1}"),
        @CompoundIndex(name = "academia_cliente_data_idx", def = "{'academiaId': 1, 'clienteId': 1, 'createdAt': -1}")
})
public class AvaliacaoFisicaDocument {

    @Id
    private String id;

    @Indexed
    private String clienteId;

    @Indexed
    private String avaliadorId;

    @Indexed
    private String academiaId;

    private String protocoloId;
    private List<MedicaoDocument> medicoes = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

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

    public String getProtocoloId() {
        return protocoloId;
    }

    public void setProtocoloId(String protocoloId) {
        this.protocoloId = protocoloId;
    }

    public String getAcademiaId() {
        return academiaId;
    }

    public void setAcademiaId(String academiaId) {
        this.academiaId = academiaId;
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
}
