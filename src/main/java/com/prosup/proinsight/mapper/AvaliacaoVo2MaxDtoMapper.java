package com.prosup.proinsight.mapper;

import com.prosup.proinsight.domain.model.MedicaoVo2;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.dto.request.MedicaoVo2Dto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AvaliacaoVo2MaxDtoMapper {

    private final TesteVo2MaxMapperRegistry testeRegistry;

    public AvaliacaoVo2MaxDtoMapper(TesteVo2MaxMapperRegistry testeRegistry) {
        this.testeRegistry = testeRegistry;
    }

    public MedicaoVo2 toMedicaoDomain(MedicaoVo2Dto dto){
        if (dto == null){
            return null;
        }

        List<TesteVo2Max> testesDomain = dto.getTestes().stream()
                .map(testeRegistry::toDomain)
                .collect(Collectors.toList());

        MedicaoVo2 medicao = new MedicaoVo2(
                Instant.now(),
                Instant.now(),
                Instant.now(),
                dto.getObservacoes(),
                dto.getTabelaClassificacaoId(),
                testesDomain
        );

        return medicao;
    }
}
