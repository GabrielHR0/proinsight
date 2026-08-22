package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.teste.TesteImc;

import java.util.List;
import java.util.Objects;

public class AvaliacaoImcContext implements AvaliacaoContext<MedicaoImc, TesteImc> {

    private final String clienteId;
    private final String avaliadorId;
    private final String tabelaClassificacaoId;
    private final MedicaoImc medicao;
    private final List<TesteImc> testes;
    private final DadosAvaliacao dadosAvaliacao;
    private final Component tabela;

    AvaliacaoImcContext(
        String clienteId,
        String avaliadorId,
        String tabelaClassificacaoId,
        MedicaoImc medicao,
        List<TesteImc> testes,
        DadosAvaliacao dadosAvaliacao,
        Component tabela
    ) {
        this.clienteId = clienteId;
        this.avaliadorId = avaliadorId;
        this.tabelaClassificacaoId = tabelaClassificacaoId;
        this.medicao = medicao;
        this.testes = List.copyOf(testes);
        this.dadosAvaliacao = dadosAvaliacao;
        this.tabela = tabela;
    }

    @Override
    public String getClienteId() { return clienteId; }

    @Override
    public String getAvaliadorId() { return avaliadorId; }

    @Override
    public MedicaoImc getMedicao() { return medicao; }

    @Override
    public List<TesteImc> getTestes() { return testes; }

    @Override
    public String getTabelaClassificacaoId() { return tabelaClassificacaoId; }

    @Override
    public DadosAvaliacao getDadosAvaliacao() { return dadosAvaliacao; }

    @Override
    public Component getTabela() { return tabela; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvaliacaoImcContext that = (AvaliacaoImcContext) o;
        return Objects.equals(clienteId, that.clienteId) &&
                Objects.equals(avaliadorId, that.avaliadorId) &&
                Objects.equals(medicao, that.medicao) &&
                Objects.equals(testes, that.testes) &&
                Objects.equals(dadosAvaliacao, that.dadosAvaliacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, avaliadorId, medicao, testes, dadosAvaliacao);
    }
}
