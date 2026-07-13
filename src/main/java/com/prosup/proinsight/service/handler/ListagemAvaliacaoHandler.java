package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.api.dto.response.AvaliacaoListaResponse;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoFisicaMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListagemAvaliacaoHandler {

    private final AvaliacaoFisicaRepository repository;
    private final AvaliacaoFisicaMapper avaliacaoMapper;

    public ListagemAvaliacaoHandler(AvaliacaoFisicaRepository repository, AvaliacaoFisicaMapper avaliacaoMapper) {
        this.repository = repository;
        this.avaliacaoMapper = avaliacaoMapper;
    }

    public List<AvaliacaoListaResponse> listarPorCliente(String clienteId) {
        return repository.findByClienteId(clienteId).stream()
            .map(avaliacaoMapper::toListaResponse)
            .toList();
    }
}
