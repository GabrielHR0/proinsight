package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;

import java.util.List;
import java.util.Objects;


public class AvaliacaoVo2MaxContext implements AvaliacaoContext<MedicaoVo2Max, TesteVo2Max> {

    private final String clienteId;
    private final String avaliadorId;
    private final MedicaoVo2Max medicao;
    private final List<TesteVo2Max> testes;
    private final DadosAvaliacao dadosAvaliacao;

    AvaliacaoVo2MaxContext(
        String clienteId,
        String avaliadorId,
        MedicaoVo2Max medicao,
        List<TesteVo2Max> testes,
        DadosAvaliacao dadosAvaliacao
    ) {
        this.clienteId = clienteId;
        this.avaliadorId = avaliadorId;
        this.medicao = medicao;
        this.testes = List.copyOf(testes);
        this.dadosAvaliacao = dadosAvaliacao;
    }

    @Override
    public String getClienteId() {
        return clienteId;
    }

    @Override
    public String getAvaliadorId() {
        return avaliadorId;
    }

    @Override
    public MedicaoVo2Max getMedicao() {
        return medicao;
    }

    @Override
    public List<TesteVo2Max> getTestes() {
        return testes;
    }

    @Override
    public String getTabelaClassificacaoId() {
        return medicao.getTabelaClassificacaoId();
    }

    @Override
    public DadosAvaliacao getDadosAvaliacao() {
        return dadosAvaliacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvaliacaoVo2MaxContext that = (AvaliacaoVo2MaxContext) o;
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

    @Override
    public String toString() {
        return "AvaliacaoVo2MaxContext{" +
                "clienteId='" + clienteId + '\'' +
                ", avaliadorId='" + avaliadorId + '\'' +
                ", tabelaId='" + getTabelaClassificacaoId() + '\'' +
                ", testes=" + testes.size() +
                ", dadosAvaliacao=" + dadosAvaliacao +
                '}';
    }
}
