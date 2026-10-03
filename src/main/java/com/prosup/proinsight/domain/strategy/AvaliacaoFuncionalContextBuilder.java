package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.MedicaoFuncional;
import com.prosup.proinsight.domain.model.composite.Component;

public class AvaliacaoFuncionalContextBuilder {

    private String clienteId;
    private String avaliadorId;
    private String tabelaClassificacaoId;
    private MedicaoFuncional medicao;
    private DadosAvaliacao dadosAvaliacao;
    private Component tabelaClassificacao;

    public AvaliacaoFuncionalContextBuilder comCliente(String clienteId) {
        this.clienteId = clienteId;
        return this;
    }

    public AvaliacaoFuncionalContextBuilder comAvaliador(String avaliadorId) {
        this.avaliadorId = avaliadorId;
        return this;
    }

    public AvaliacaoFuncionalContextBuilder comTabelaClassificacaoId(String tabelaClassificacaoId) {
        this.tabelaClassificacaoId = tabelaClassificacaoId;
        return this;
    }

    public AvaliacaoFuncionalContextBuilder comMedicao(MedicaoFuncional medicao) {
        if (medicao == null) throw new IllegalArgumentException("Medicao funcional não pode ser nula");
        this.medicao = medicao;
        return this;
    }

    public AvaliacaoFuncionalContextBuilder comDadosAvaliacao(DadosAvaliacao dadosAvaliacao) {
        this.dadosAvaliacao = dadosAvaliacao;
        return this;
    }

    public AvaliacaoFuncionalContextBuilder comTabelaClassificacao(Component tabelaClassificacao) {
        this.tabelaClassificacao = tabelaClassificacao;
        return this;
    }

    public AvaliacaoFuncionalContext build() {
        if (clienteId == null || clienteId.isBlank())
            throw new IllegalArgumentException("ClienteId é obrigatório");
        if (avaliadorId == null || avaliadorId.isBlank())
            throw new IllegalArgumentException("AvaliadorId é obrigatório");
        if (tabelaClassificacaoId == null || tabelaClassificacaoId.isBlank())
            throw new IllegalArgumentException("tabelaClassificacaoId é obrigatório");
        if (medicao == null)
            throw new IllegalArgumentException("Medicao funcional é obrigatória");
        if (medicao.getTestes() == null || medicao.getTestes().isEmpty())
            throw new IllegalArgumentException("Medicao funcional deve ter testes associados");
        if (tabelaClassificacao == null)
            throw new IllegalArgumentException("tabelaClassificacao é obrigatória");

        return new AvaliacaoFuncionalContext(clienteId, avaliadorId, tabelaClassificacaoId, medicao, medicao.getTestes(), dadosAvaliacao, tabelaClassificacao);
    }
}
