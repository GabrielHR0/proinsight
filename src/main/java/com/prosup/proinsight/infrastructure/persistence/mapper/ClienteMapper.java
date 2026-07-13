package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.request.ClienteRequest;
import com.prosup.proinsight.api.dto.response.ClienteResponse;
import com.prosup.proinsight.domain.model.Cliente;
import com.prosup.proinsight.domain.model.Endereco;
import com.prosup.proinsight.infrastructure.persistence.document.ClienteDocument;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toDomain(ClienteDocument doc) {
        if (doc == null) return null;

        return new Cliente(
                doc.getId(),
                doc.getFullName(),
                doc.getEmail(),
                doc.getPhone(),
                doc.getCpf(),
                doc.getEndereco(),
                doc.getAcademiaId(),
                doc.getAvaliadorId()
        );
    }

    public ClienteDocument toDocument(Cliente domain) {
        if (domain == null) return null;

        return new ClienteDocument(
                domain.getId(),
                domain.getFullName(),
                domain.getEmail(),
                domain.getPhone(),
                domain.getCpf(),
                domain.getEndereco(),
                domain.getAcademiaId(),
                domain.getAvaliadorId()
        );
    }

    public ClienteDocument toDocument(ClienteRequest req) {
        if (req == null) return null;

        Endereco endereco = new Endereco(
                req.getRua(),
                req.getNumero(),
                req.getCidade(),
                req.getEstado(),
                req.getCep()
        );

        return new ClienteDocument(
                null,
                req.getFullName(),
                req.getEmail(),
                req.getPhone(),
                req.getCpf(),
                endereco,
                req.getAcademiaId(),
                req.getAvaliadorId()
        );
    }

    public ClienteResponse toResponse(ClienteDocument doc) {
        if (doc == null) return null;

        return new ClienteResponse(
                doc.getId(),
                doc.getFullName(),
                doc.getEmail(),
                doc.getPhone(),
                doc.getCpf(),
                doc.getEndereco(),
                doc.getAcademiaId(),
                doc.getAvaliadorId()
        );
    }
}
