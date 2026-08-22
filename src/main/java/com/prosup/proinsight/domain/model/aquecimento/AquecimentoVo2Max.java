package com.prosup.proinsight.domain.model.aquecimento;

import com.prosup.proinsight.domain.enums.Protocolo;

/**
 * Classe abstrata para aquecimentos de VO2Max.
 * Diferente de TesteVo2Max, NÃO calcula nada.
 * Apenas registra dados do aquecimento (distância, tempo, etc.)
 */
public abstract class AquecimentoVo2Max implements Aquecimento {

    private final String codigo = gerarCodigo();

    protected Protocolo protocolo;
    protected Double tempoMinutos;
    protected String observacoes;

    public AquecimentoVo2Max() {}

    public AquecimentoVo2Max(Protocolo protocolo) {
        this.protocolo = protocolo;
    }

    @Override
    public String getCriterio() {
        return protocolo != null ? protocolo.name() : null;
    }

    public Protocolo getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(Protocolo protocolo) {
        this.protocolo = protocolo;
    }

    public Double getTempoMinutos() {
        return tempoMinutos;
    }

    public void setTempoMinutos(Double tempoMinutos) {
        this.tempoMinutos = tempoMinutos;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
