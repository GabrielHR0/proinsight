package com.prosup.proinsight.infrastructure.persistence.document;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.teste.Teste;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "tipo"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MedicaoVo2MaxDocument.class, name = "VO2_MAX"),
    @JsonSubTypes.Type(value = MedicaoImcDocument.class, name = "IMC"),
    @JsonSubTypes.Type(value = MedicaoBioimpedanciaDocument.class, name = "BIOIMPEDANCIA"),
})
public abstract class MedicaoDocument {

    private MedicaoTipo tipo;
    private Instant medidoEm;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String observacoes;
    private List<Teste> testes;

    protected MedicaoDocument() {
    }

    protected MedicaoDocument(MedicaoTipo tipo) {
        this.tipo = tipo;
    }

    protected MedicaoDocument(MedicaoTipo tipo, List<Teste> testes) {
        this.tipo = tipo;
        this.testes = testes;
    }

    public List<Teste> getTestes() {
        return testes;
    }

    public void setTestes(List<Teste> testes) {
        this.testes = testes;
    }

    public MedicaoTipo getTipo() {
        return tipo;
    }

    public void setTipo(MedicaoTipo tipo) {
        this.tipo = tipo;
    }

    public Instant getMedidoEm() {
        return medidoEm;
    }

    public void setMedidoEm(Instant medidoEm) {
        this.medidoEm = medidoEm;
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

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
