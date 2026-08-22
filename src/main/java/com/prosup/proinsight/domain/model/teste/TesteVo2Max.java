package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.Protocolo;

public abstract class TesteVo2Max implements Teste {

    private final String codigo = gerarCodigo();

    protected Protocolo protocolo;
    protected Double valorClassificacao;

    public TesteVo2Max() {}

    public TesteVo2Max(Protocolo protocolo, Double valorClassificacao) {
        this.protocolo = protocolo;
        this.valorClassificacao = valorClassificacao;
    }

    public abstract Double calcularVo2Max(DadosAvaliacao dados);

    @Override
    public String getCriterio() {
        return protocolo != null ? protocolo.name() : null;
    }

    @Override
    public String getValorClassificacao() {
        return valorClassificacao != null ? String.valueOf(valorClassificacao) : null;
    }

    public Protocolo getProtocolo() {
        return protocolo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setProtocolo(Protocolo protocolo) {
        this.protocolo = protocolo;
    }

    public Double getValorClassificacaoObj() {
        return valorClassificacao;
    }

    public void setValorClassificacao(Double valorClassificacao) {
        this.valorClassificacao = valorClassificacao;
    }
}
