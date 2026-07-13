package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.composite.Component;

public class AvaliacaoVo2MaxContextBuilder {

    private String clienteId;
    private String avaliadorId;
    private String tabelaClassificacaoId;
    private MedicaoVo2Max medicao;
    private DadosAvaliacao dadosAvaliacao;
    private Component tabelaClassificacao;

    public AvaliacaoVo2MaxContextBuilder comCliente(String clienteId) {
        this.clienteId = clienteId;
        return this;
    }

    public AvaliacaoVo2MaxContextBuilder comAvaliador(String avaliadorId) {
        this.avaliadorId = avaliadorId;
        return this;
    }

    public AvaliacaoVo2MaxContextBuilder comTabelaClassificacaoId(String tabelaClassificacaoId) {
        this.tabelaClassificacaoId = tabelaClassificacaoId;
        return this;
    }

    public AvaliacaoVo2MaxContextBuilder comMedicao(MedicaoVo2Max medicao) {
        if (medicao == null) {
            throw new IllegalArgumentException("Medicao VO2Max não pode ser nula");
        }
        this.medicao = medicao;
        return this;
    }

    public AvaliacaoVo2MaxContextBuilder comDadosAvaliacao(DadosAvaliacao dadosAvaliacao) {
        this.dadosAvaliacao = dadosAvaliacao;
        return this;
    }

    public AvaliacaoVo2MaxContextBuilder comTabelaClassificacao(Component tabelaClassificacao) {
        this.tabelaClassificacao = tabelaClassificacao;
        return this;
    }

    public AvaliacaoVo2MaxContext build() {
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("ClienteId é obrigatório e não pode ser vazio");
        }

        if (avaliadorId == null || avaliadorId.isBlank()) {
            throw new IllegalArgumentException("AvaliadorId é obrigatório e não pode ser vazio");
        }

        if (tabelaClassificacaoId == null || tabelaClassificacaoId.isBlank()) {
            throw new IllegalArgumentException(
                "tabelaClassificacaoId é obrigatório e não pode ser vazio"
            );
        }

        if (medicao == null) {
            throw new IllegalArgumentException("Medicao VO2Max é obrigatória");
        }

        if (medicao.getTestes() == null || medicao.getTestes().isEmpty()) {
            throw new IllegalArgumentException("Medicao VO2Max deve ter testes associados");
        }

        for (int i = 0; i < medicao.getTestes().size(); i++) {
            if (medicao.getTestes().get(i) == null) {
                throw new IllegalArgumentException(
                    "Teste na posição " + i + " é nulo"
                );
            }
        }

        if (tabelaClassificacao == null) {
            throw new IllegalArgumentException("tabelaClassificacao é obrigatório e não pode ser vazio");
        }

        return new AvaliacaoVo2MaxContext(clienteId, avaliadorId, tabelaClassificacaoId, medicao, medicao.getTestes(), dadosAvaliacao, tabelaClassificacao);
    }
}
