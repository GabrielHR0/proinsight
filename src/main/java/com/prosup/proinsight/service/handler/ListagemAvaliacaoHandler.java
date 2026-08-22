package com.prosup.proinsight.service.handler;

import com.prosup.proinsight.api.dto.response.AvaliacaoHistoricoResponse;
import com.prosup.proinsight.service.HistoricoAvaliacoesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListagemAvaliacaoHandler {

    private final HistoricoAvaliacoesService historicoAvaliacoesService;

    public ListagemAvaliacaoHandler(HistoricoAvaliacoesService historicoAvaliacoesService) {
        this.historicoAvaliacoesService = historicoAvaliacoesService;
    }

    public List<AvaliacaoHistoricoResponse> listarPorCliente(String clienteId) {
        return historicoAvaliacoesService.listarPorCliente(clienteId);
    }
}