package com.prosup.proinsight.service;

import com.prosup.proinsight.infrastructure.persistence.repository.AvaliadorRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.domain.strategy.AvaliacaoContext;
import com.prosup.proinsight.domain.model.Medicao;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.Teste;
import org.springframework.stereotype.Service;


@Service
public class AvaliacaoService {

    private final ClienteRepository clienteRepository;
    private final AvaliadorRepository avaliadorRepository;

    public AvaliacaoService(
            ClienteRepository clienteRepository,
            AvaliadorRepository avaliadorRepository
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
