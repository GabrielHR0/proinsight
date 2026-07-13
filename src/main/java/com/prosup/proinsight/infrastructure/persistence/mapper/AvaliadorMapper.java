package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.request.AvaliadorRequest;
import com.prosup.proinsight.api.dto.response.AvaliadorResponse;
import com.prosup.proinsight.domain.model.Avaliador;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliadorDocument;
import org.springframework.stereotype.Component;

@Component
public class AvaliadorMapper {

    public AvaliadorDocument toDocument(AvaliadorRequest req) {
        if (req == null) return null;
        AvaliadorDocument d = new AvaliadorDocument();
        d.setUserId(req.getUserId());
        d.setAcademiaId(req.getAcademiaId());
        d.setCref(req.getCref());
        d.setFirstName(req.getFirstName());
        d.setLastName(req.getLastName());
        d.setEmail(req.getEmail());
        d.setTelefone(req.getTelefone());
        d.setCpf(req.getCpf());
        return d;
    }

    public Avaliador toDomain(AvaliadorDocument d) {
        if (d == null) return null;
        return new Avaliador(
                d.getId(),
                d.getUserId(),
                d.getAcademiaId(),
                d.getCref(),
                d.getFirstName(),
                d.getLastName(),
                d.getEmail(),
                d.getTelefone(),
                d.getCpf());
    }

    public AvaliadorResponse toResponse(Avaliador domain) {
        if (domain == null) return null;
        return new AvaliadorResponse(
                domain.getId(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail(),
                domain.getTelefone(),
                domain.getCpf(),
                domain.getCref(),
                domain.getAcademiaId());
    }
}
