package com.prosup.proinsight.service;

import com.prosup.proinsight.adapter.out.persistence.MongoAvaliadorDataRepository;
import com.prosup.proinsight.adapter.out.persistence.MongoClienteDataRepository;
import com.prosup.proinsight.domain.avalicao_strategy.AvaliacaoContext;
import com.prosup.proinsight.domain.model.Medicao;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;
import org.springframework.stereotype.Service;


@Service
public class AvaliacaoService {

    private final MongoClienteDataRepository clienteRepository;
    private final MongoAvaliadorDataRepository avaliadorRepository;

    public AvaliacaoService(
            MongoClienteDataRepository clienteRepository,
            MongoAvaliadorDataRepository avaliadorRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.avaliadorRepository = avaliadorRepository;
    }

    public <M extends Medicao, T extends Teste> void salvarAvaliacao(
        AvaliacaoContext<M, T> contexto,
        Leaf resultado
    ) {
        clienteRepository.findById(contexto.getClienteId())
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        avaliadorRepository.findById(contexto.getAvaliadorId())
            .orElseThrow(() -> new RuntimeException("Avaliador não encontrado"));


    }
}
