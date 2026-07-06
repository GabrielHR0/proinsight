package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

public abstract class TesteVo2Max implements Teste {

    private final String codigo = gerarCodigo();

    protected ProtocoloVo2Max protocolo;
    protected Double valorClassificacao;

    public TesteVo2Max() {}

    public TesteVo2Max(ProtocoloVo2Max protocolo, Double valorClassificacao) {
        this.protocolo = protocolo;
        this.valorClassificacao = valorClassificacao;
    }

    @Override
    public String getCriterio() {
        return protocolo != null ? protocolo.name() : null;
    }

    @Override
    public String getValorClassificacao() {
        return valorClassificacao != null ? String.valueOf(valorClassificacao) : null;
    }

    public ProtocoloVo2Max getProtocolo() {
        return protocolo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }

    public Double getValorClassificacaoObj() {
        return valorClassificacao;
    }

    public void setValorClassificacao(Double valorClassificacao) {
        this.valorClassificacao = valorClassificacao;
    }
}
