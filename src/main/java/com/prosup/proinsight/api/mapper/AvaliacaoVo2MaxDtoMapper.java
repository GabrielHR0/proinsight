package com.prosup.proinsight.api.mapper;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.api.dto.request.MedicaoVo2MaxDto;
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

    public MedicaoVo2Max toMedicaoDomain(MedicaoVo2MaxDto dto){
        if (dto == null){
            return null;
        }

        List<TesteVo2Max> testesDomain = dto.getTestes().stream()
                .map(testeRegistry::toDomain)
                .collect(Collectors.toList());

        MedicaoVo2Max medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX,
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
