package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.response.TabelaClassificacaoResponse;
import com.prosup.proinsight.infrastructure.persistence.document.TabelaClassificacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.TabelaClassificacaoMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TabelasClassificacaoService {

    private final TabelaClassificacaoRepository tabelaClassificacaoRepository;
    private final TabelaClassificacaoMapper tabelaClassificacaoMapper;

    public TabelasClassificacaoService(
            TabelaClassificacaoRepository tabelaClassificacaoRepository,
            TabelaClassificacaoMapper tabelaClassificacaoMapper) {
        this.tabelaClassificacaoRepository = tabelaClassificacaoRepository;
        this.tabelaClassificacaoMapper = tabelaClassificacaoMapper;
    }

    public Optional<TabelaClassificacaoDocument> find(String id){
        return tabelaClassificacaoRepository.findById(id);
    }

    public List<TabelaClassificacaoResponse> findAll() {
        return this.tabelaClassificacaoRepository.findAll()
                .stream()
                .map(tabelaClassificacaoMapper::toResponse)
                .toList();
    }
}
