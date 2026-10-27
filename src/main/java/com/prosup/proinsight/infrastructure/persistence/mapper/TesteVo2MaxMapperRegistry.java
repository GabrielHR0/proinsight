package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.request.TesteVo2MaxDto;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxEsteiraIncremental;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class TesteVo2MaxMapperRegistry {

    private final Map<Protocolo, Function<TesteVo2MaxDto, TesteVo2Max>>
            mappers = new HashMap<>();

    public TesteVo2MaxMapperRegistry(){
        registrarMapeadores();
    }

    private void registrarMapeadores(){
        mappers.put(Protocolo.COOPER, dto -> {
            Double resultado = validarResultado(dto, "Cooper");
            if (resultado != Math.rint(resultado)) {
                throw new IllegalArgumentException(
                        "Teste Vo2 Max Cooper: distância em metros deve ser um número inteiro");
            }
            return new TesteVo2MaxCooper(resultado.intValue());
        });

        mappers.put(Protocolo.ESTEIRA_INCREMENTAL, dto -> new TesteVo2MaxEsteiraIncremental(
            validarResultado(dto, "Esteira Incremental"),
            dto.getInclinacaoPercent()
        ));
    }

    private static Double validarResultado(TesteVo2MaxDto dto, String protocolo) {
        Double resultado = dto.getResultado();
        if (resultado == null || resultado.isNaN() || resultado.isInfinite()) {
            throw new IllegalArgumentException(
                    "Teste Vo2 Max " + protocolo + ": resultado é obrigatório e deve ser numérico");
        }
        if (resultado < 0) {
            throw new IllegalArgumentException(
                    "Teste Vo2 Max " + protocolo + ": resultado não pode ser negativo");
        }
        return resultado;
    }

    public TesteVo2Max toDomain(TesteVo2MaxDto dto){
        if (dto == null || dto.getProtocolo() == null) {
            throw  new IllegalArgumentException("Teste Vo2 Max: DTO ou protocolo não pode ser nulos");
        }

        var mapper = mappers.get(dto.getProtocolo());
        if (mapper == null) {
            throw new IllegalArgumentException(
                    "Protocolo não suportado: " + dto.getProtocolo());
        }

        return mapper.apply(dto);
    }
}
