package com.prosup.proinsight.api.mapper;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxRockport;
import com.prosup.proinsight.api.dto.request.TesteVo2MaxDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class TesteVo2MaxMapperRegistry {

    private final Map<ProtocoloVo2Max, Function<TesteVo2MaxDto, TesteVo2Max>>
            mappers = new HashMap<>();

    public TesteVo2MaxMapperRegistry(){
        registrarMapeadores();
    }

    private void registrarMapeadores(){
        mappers.put(ProtocoloVo2Max.COOPER, dto -> {
            int distanciaMetros = (int) dto.getResultado();
            return new TesteVo2MaxCooper(distanciaMetros);
        });

        mappers.put(ProtocoloVo2Max.ROCKPORT, dto -> new TesteVo2MaxRockport(
            dto.getResultado(),
            dto.getFrequenciaCardiaca(),
            dto.getPesoKg()
        ));
    }

    public TesteVo2Max toDomain(TesteVo2MaxDto dto){
        if (dto == null || dto.getProtocolo() == null) {
            throw  new IllegalArgumentException("Teste Vo2 Max: DTO ou protocolo não pode ser nulos");
        }

        ProtocoloVo2Max protocolo = dto.getProtocolo();
        Function<TesteVo2MaxDto, TesteVo2Max> mapeador =
                mappers.get(protocolo);

        if (mapeador == null) {
            throw new IllegalArgumentException(
                    "Protocolo VO2Max não mapeado:" + protocolo +
                    ". Protocolos disponíveis: " + mappers.keySet()
            );
        }

        return mapeador.apply(dto);
    }

}
