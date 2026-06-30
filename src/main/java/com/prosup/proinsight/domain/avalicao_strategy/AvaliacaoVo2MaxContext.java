package com.prosup.proinsight.domain.avalicao_strategy;

import com.prosup.proinsight.domain.model.MedicaoVo2;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;

import java.util.List;
import java.util.Objects;


public class AvaliacaoVo2MaxContext implements AvaliacaoContext<MedicaoVo2, TesteVo2Max> {
    
    private final String clienteId;
    private final String avaliadorId;
    private final MedicaoVo2 medicao;
    private final List<TesteVo2Max> testes;
    

    AvaliacaoVo2MaxContext(
        String clienteId,
        String avaliadorId,
        MedicaoVo2 medicao,
        List<TesteVo2Max> testes
    ) {
        this.clienteId = clienteId;
        this.avaliadorId = avaliadorId;
        this.medicao = medicao;
        this.testes = List.copyOf(testes);
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
    public MedicaoVo2 getMedicao() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvaliacaoVo2MaxContext that = (AvaliacaoVo2MaxContext) o;
        return Objects.equals(clienteId, that.clienteId) &&
                Objects.equals(avaliadorId, that.avaliadorId) &&
                Objects.equals(medicao, that.medicao) &&
                Objects.equals(testes, that.testes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(clienteId, avaliadorId, medicao, testes);
    }
    
    @Override
    public String toString() {
        return "AvaliacaoVo2MaxContext{" +
                "clienteId='" + clienteId + '\'' +
                ", avaliadorId='" + avaliadorId + '\'' +
                ", tabelaId='" + getTabelaClassificacaoId() + '\'' +
                ", testes=" + testes.size() +
                '}';
    }
}
