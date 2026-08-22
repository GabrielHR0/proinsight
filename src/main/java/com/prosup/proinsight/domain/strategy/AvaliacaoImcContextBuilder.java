package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.composite.Component;

public class AvaliacaoImcContextBuilder {

    private String clienteId;
    private String avaliadorId;
    private String tabelaClassificacaoId;
    private MedicaoImc medicao;
    private DadosAvaliacao dadosAvaliacao;
    private Component tabelaClassificacao;

    public AvaliacaoImcContextBuilder comCliente(String clienteId) {
        this.clienteId = clienteId;
        return this;
    }

    public AvaliacaoImcContextBuilder comAvaliador(String avaliadorId) {
        this.avaliadorId = avaliadorId;
        return this;
    }

    public AvaliacaoImcContextBuilder comTabelaClassificacaoId(String tabelaClassificacaoId) {
        this.tabelaClassificacaoId = tabelaClassificacaoId;
        return this;
    }

    public AvaliacaoImcContextBuilder comMedicao(MedicaoImc medicao) {
        if (medicao == null) throw new IllegalArgumentException("Medicao IMC não pode ser nula");
        this.medicao = medicao;
        return this;
    }

    public AvaliacaoImcContextBuilder comDadosAvaliacao(DadosAvaliacao dadosAvaliacao) {
        this.dadosAvaliacao = dadosAvaliacao;
        return this;
    }

    public AvaliacaoImcContextBuilder comTabelaClassificacao(Component tabelaClassificacao) {
        this.tabelaClassificacao = tabelaClassificacao;
        return this;
    }

    public AvaliacaoImcContext build() {
        if (clienteId == null || clienteId.isBlank())
            throw new IllegalArgumentException("ClienteId é obrigatório");
        if (avaliadorId == null || avaliadorId.isBlank())
            throw new IllegalArgumentException("AvaliadorId é obrigatório");
        if (tabelaClassificacaoId == null || tabelaClassificacaoId.isBlank())
            throw new IllegalArgumentException("tabelaClassificacaoId é obrigatório");
        if (medicao == null)
            throw new IllegalArgumentException("Medicao IMC é obrigatória");
        if (medicao.getTestes() == null || medicao.getTestes().isEmpty())
            throw new IllegalArgumentException("Medicao IMC deve ter testes associados");
        if (tabelaClassificacao == null)
            throw new IllegalArgumentException("tabelaClassificacao é obrigatória");

        return new AvaliacaoImcContext(clienteId, avaliadorId, tabelaClassificacaoId, medicao, medicao.getTestes(), dadosAvaliacao, tabelaClassificacao);
    }
}
