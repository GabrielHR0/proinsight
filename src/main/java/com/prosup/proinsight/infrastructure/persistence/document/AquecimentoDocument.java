package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.domain.enums.Protocolo;

/**
 * Document para persistir dados de aquecimento.
 * Apenas registra o que aconteceu — não calcula nada.
 */
public class AquecimentoDocument {

    private Protocolo protocolo;
    private String descricao;
    private String observacoes;

    private Integer distanciaMetros;
    private Double tempoMinutos;
    private Double velocidadeKmh;
    private Double inclinacaoPercent;
    private Integer duracaoSegundos;

    public AquecimentoDocument() {
    }

    public AquecimentoDocument(Protocolo protocolo, String descricao) {
        this.protocolo = protocolo;
        this.descricao = descricao;
    }

    public Protocolo getProtocolo() { return protocolo; }
    public void setProtocolo(Protocolo protocolo) { this.protocolo = protocolo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Integer getDistanciaMetros() { return distanciaMetros; }
    public void setDistanciaMetros(Integer distanciaMetros) { this.distanciaMetros = distanciaMetros; }

    public Double getTempoMinutos() { return tempoMinutos; }
    public void setTempoMinutos(Double tempoMinutos) { this.tempoMinutos = tempoMinutos; }

    public Double getVelocidadeKmh() { return velocidadeKmh; }
    public void setVelocidadeKmh(Double velocidadeKmh) { this.velocidadeKmh = velocidadeKmh; }

    public Double getInclinacaoPercent() { return inclinacaoPercent; }
    public void setInclinacaoPercent(Double inclinacaoPercent) { this.inclinacaoPercent = inclinacaoPercent; }

    public Integer getDuracaoSegundos() { return duracaoSegundos; }
    public void setDuracaoSegundos(Integer duracaoSegundos) { this.duracaoSegundos = duracaoSegundos; }
}
