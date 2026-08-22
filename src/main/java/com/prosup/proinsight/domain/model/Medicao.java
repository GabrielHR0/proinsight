package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;

import java.time.Instant;
import java.util.List;

public abstract class Medicao<T extends Teste> {

    private MedicaoTipo tipo;
    private Instant medidoEm;
    private Instant createdAt;
    private Instant updatedAt;
    private String observacoes;
    private List<T> testes;

    protected Medicao() {
    }

    protected Medicao(MedicaoTipo tipo) {
        this.tipo = tipo;
    }

    public Medicao(MedicaoTipo tipo, Instant medidoEm, Instant createdAt, Instant updatedAt, String observacoes, List<T> testes) {
        this.tipo = tipo;
        this.medidoEm = medidoEm;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.observacoes = observacoes;
        this.testes = testes;
    }

    public List<T> getTestes() {
        return testes;
    }

    public void setTestes(List<T> testes) {
        this.testes = testes;
    }

    public void addTestes(T teste){
        this.testes.add(teste);
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
