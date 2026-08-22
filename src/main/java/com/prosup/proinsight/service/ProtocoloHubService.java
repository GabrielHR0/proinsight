package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.response.ProtocoloDetalheResponse;
import com.prosup.proinsight.api.dto.response.ProtocoloResumoResponse;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UsuarioProtocoloFavoritoDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UsuarioProtocoloFavoritoRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ProtocoloHubService {

    private final ProtocoloAvaliacaoRepository protocoloRepository;
    private final UsuarioProtocoloFavoritoRepository favoritoRepository;

    public ProtocoloHubService(ProtocoloAvaliacaoRepository protocoloRepository,
                               UsuarioProtocoloFavoritoRepository favoritoRepository) {
        this.protocoloRepository = protocoloRepository;
        this.favoritoRepository = favoritoRepository;
    }

    public Map<String, Object> getHub(String userId) {
        var favoritos = favoritoRepository.findByUserId(userId);
        var favoritoIds = favoritos.stream()
                .map(UsuarioProtocoloFavoritoDocument::getProtocoloId)
                .toList();

        var todosProtocolos = protocoloRepository.findAll();
        var protocolosFavoritos = favoritoIds.isEmpty()
                ? List.of()
                : protocoloRepository.findAllById(favoritoIds).stream()
                    .map(this::toResumo)
                    .toList();

        Map<String, List<ProtocoloResumoResponse>> porCategoria = new LinkedHashMap<>();
        for (var protocolo : todosProtocolos) {
            String categoria = protocolo.getCategoria() != null ? protocolo.getCategoria() : "OUTROS";
            porCategoria.computeIfAbsent(categoria, k -> new java.util.ArrayList<>())
                    .add(toResumo(protocolo));
        }

        Map<String, Object> hub = new LinkedHashMap<>();
        hub.put("favoritos", protocolosFavoritos);
        hub.put("porCategoria", porCategoria);
        return hub;
    }

    public List<ProtocoloResumoResponse> listarTodos() {
        return protocoloRepository.findAll().stream()
                .map(this::toResumo)
                .toList();
    }

    public ProtocoloDetalheResponse getDetalhe(String protocoloId) {
        var doc = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new NoSuchElementException("Protocolo não encontrado: " + protocoloId));
        return toDetalhe(doc);
    }

    public void favoritar(String userId, String protocoloId) {
        if (!protocoloRepository.existsById(protocoloId)) {
            throw new NoSuchElementException("Protocolo não encontrado: " + protocoloId);
        }
        if (!favoritoRepository.existsByUserIdAndProtocoloId(userId, protocoloId)) {
            favoritoRepository.save(new UsuarioProtocoloFavoritoDocument(userId, protocoloId));
        }
    }

    public void desfavoritar(String userId, String protocoloId) {
        favoritoRepository.deleteByUserIdAndProtocoloId(userId, protocoloId);
    }

    public List<ProtocoloResumoResponse> listarFavoritos(String userId) {
        var favoritos = favoritoRepository.findByUserId(userId);
        var protocoloIds = favoritos.stream()
                .map(UsuarioProtocoloFavoritoDocument::getProtocoloId)
                .toList();

        return protocoloRepository.findAllById(protocoloIds).stream()
                .map(this::toResumo)
                .toList();
    }

    public boolean isFavorito(String userId, String protocoloId) {
        return favoritoRepository.existsByUserIdAndProtocoloId(userId, protocoloId);
    }

    private ProtocoloResumoResponse toResumo(ProtocoloAvaliacaoDocument doc) {
        return new ProtocoloResumoResponse(
                doc.getId(),
                doc.getNome(),
                doc.getCategoria(),
                doc.getPadrao(),
                doc.getDescricao(),
                doc.getUnidadeMedida()
        );
    }

    private ProtocoloDetalheResponse toDetalhe(ProtocoloAvaliacaoDocument doc) {
        return new ProtocoloDetalheResponse(
                doc.getId(),
                doc.getNome(),
                doc.getCategoria(),
                doc.getPadrao(),
                doc.getStrategyKey(),
                doc.getTabelaClassificacaoId(),
                doc.getDescricao(),
                doc.getComoRealizar(),
                doc.getCalculadora(),
                doc.getReferenciaBibliografica(),
                doc.getUnidadeMedida(),
                doc.getTempoMinimoSegundos(),
                doc.getTempoMaximoSegundos(),
                doc.getEquipamentoNecessario(),
                doc.getCriteriosExclusao(),
                doc.getObservacoes(),
                doc.getCreatedAt()
        );
    }
}
