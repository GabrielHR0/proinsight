package com.prosup.proinsight.validator;

import com.prosup.proinsight.dto.request.AvaliacaoVo2MaxRequest;
import com.prosup.proinsight.dto.request.MedicaoVo2Dto;
import com.prosup.proinsight.exception.ValidacaoException;
import com.prosup.proinsight.domain.model.MedicaoVo2;
import org.springframework.stereotype.Component;


@Component
public class AvaliacaoVo2MaxValidator {
    
    private final RequestValidator requestValidator;
    
    private static final int MAX_ID_LENGTH = 36;

    public AvaliacaoVo2MaxValidator(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }
    

    public void validarRequisicao(AvaliacaoVo2MaxRequest request) {
        requestValidator.validarObjetoNaoNulo(request, "AvaliacaoVo2MaxRequest");
        
        validarId(request.getClienteId(), "clienteId");
        validarId(request.getAvaliadorId(), "avaliadorId");
        validarId(request.getAvaliacaoFisicaId(),"avaliacaoFisicaId");
        
        requestValidator.validarObjetoNaoNulo(request.getMedicaoVo2Dto(), "medicaoVo2Max");
        validarEstruturaMedicaoVo2(request.getMedicaoVo2Dto());
    }

    private void validarId(String id, String nomeCampo) {
        requestValidator.validarStringNaoVazia(id, nomeCampo);

        if (id.length() > MAX_ID_LENGTH) {
            throw new ValidacaoException(
                nomeCampo + " não pode ter mais de " + MAX_ID_LENGTH + " caracteres"
            );
        }
        
        if (!id.matches("[a-zA-Z0-9_-]+")) {
            throw new ValidacaoException(
                nomeCampo + " contém caracteres inválidos. Use apenas letras, números, hífens e underscores"
            );
        }
    }
    

    private void validarEstruturaMedicaoVo2(MedicaoVo2Dto medicao) {
        //todo implementar validação da medição vo2
    }
}
