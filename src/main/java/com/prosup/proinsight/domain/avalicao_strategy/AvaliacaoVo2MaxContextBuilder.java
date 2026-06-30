package com.prosup.proinsight.domain.avalicao_strategy;

import com.prosup.proinsight.domain.model.MedicaoVo2;

public class AvaliacaoVo2MaxContextBuilder {
    
    private String clienteId;
    private String avaliadorId;
    private MedicaoVo2 medicao;


    public AvaliacaoVo2MaxContextBuilder comCliente(String clienteId) {
        this.clienteId = clienteId;
        return this;
    }

    public AvaliacaoVo2MaxContextBuilder comAvaliador(String avaliadorId) {
        this.avaliadorId = avaliadorId;
        return this;
    }

    public AvaliacaoVo2MaxContextBuilder comMedicao(MedicaoVo2 medicao) {
        if (medicao == null) {
            throw new IllegalArgumentException("Medicao VO2Max não pode ser nula");
        }
        this.medicao = medicao;
        return this;
    }

    public AvaliacaoVo2MaxContext build() {
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("ClienteId é obrigatório e não pode ser vazio");
        }
        
        if (avaliadorId == null || avaliadorId.isBlank()) {
            throw new IllegalArgumentException("AvaliadorId é obrigatório e não pode ser vazio");
        }
        
        if (medicao == null) {
            throw new IllegalArgumentException("Medicao VO2Max é obrigatória");
        }
        
        if (medicao.getTabelaClassificacaoId() == null || medicao.getTabelaClassificacaoId().isBlank()) {
            throw new IllegalArgumentException(
                "Medicao VO2Max deve ter um ID de tabela de classificação válido"
            );
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
        
        return new AvaliacaoVo2MaxContext(clienteId, avaliadorId, medicao, medicao.getTestes());
    }
}
