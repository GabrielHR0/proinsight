package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.request.AcademiaRequest;
import com.prosup.proinsight.api.dto.response.AcademiaResponse;
import com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument;
import org.springframework.stereotype.Component;

@Component
public class AcademiaMapper {

    public AcademiaDocument toDocument(AcademiaRequest req) {
        if (req == null) return null;
        AcademiaDocument d = new AcademiaDocument();
        d.setOwnerId(req.getOwnerId());
        d.setNomeFantasia(req.getNomeFantasia());
        d.setRazaoSocial(req.getRazaoSocial());
        d.setCnpj(req.getCnpj());
        if (req.getEndereco() != null) {
            var er = req.getEndereco();
            d.setEndereco(new AcademiaDocument.Endereco(er.getRua(), er.getNumero(), er.getCidade(), er.getEstado(), er.getCep()));
        }
        d.setTelefone(req.getTelefone());
        return d;
    }

    public AcademiaResponse toResponse(AcademiaDocument d) {
        if (d == null) return null;
        AcademiaResponse.EnderecoResponse endereco = null;
        if (d.getEndereco() != null) {
            var ed = d.getEndereco();
            endereco = new AcademiaResponse.EnderecoResponse(ed.getRua(), ed.getNumero(), ed.getCidade(), ed.getEstado(), ed.getCep());
        }
        return new AcademiaResponse(
                d.getId(),
                d.getOwnerId(),
                d.getNomeFantasia(),
                d.getRazaoSocial(),
                d.getCnpj(),
                endereco,
                d.getTelefone(),
                d.getCreatedAt(),
                d.getUpdatedAt());
    }
}
