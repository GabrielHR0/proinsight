package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.request.TesteVo2MaxDto;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxRockport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TesteVo2MaxMapperRegistryTest {

    private final TesteVo2MaxMapperRegistry registry = new TesteVo2MaxMapperRegistry();

    @Test
    void shouldConvertCooperDtoToTesteVo2MaxCooper() {
        var dto = new TesteVo2MaxDto(ProtocoloVo2Max.COOPER, 3000.0);

        TesteVo2Max result = registry.toDomain(dto);

        assertThat(result).isInstanceOf(TesteVo2MaxCooper.class);
        assertThat(((TesteVo2MaxCooper) result).getDistanciaMetros()).isEqualTo(3000);
    }

    @Test
    void shouldConvertRockportDtoToTesteVo2MaxRockport() {
        var dto = new TesteVo2MaxDto(ProtocoloVo2Max.ROCKPORT, 12.5);
        dto.setFrequenciaCardiaca(145);
        dto.setPesoKg(70.0);

        TesteVo2Max result = registry.toDomain(dto);

        assertThat(result).isInstanceOf(TesteVo2MaxRockport.class);
        var rockport = (TesteVo2MaxRockport) result;
        assertThat(rockport.getTempoMinutos()).isEqualTo(12.5);
        assertThat(rockport.getFrequenciaCardiaca()).isEqualTo(145);
        assertThat(rockport.getPesoKg()).isEqualTo(70.0);
    }

    @Test
    void shouldThrowWhenProtocoloNotMapped() {
        var dto = new TesteVo2MaxDto(null, 0);

        assertThrows(IllegalArgumentException.class, () -> registry.toDomain(dto));
    }

    @Test
    void shouldThrowWhenDtoIsNull() {
        assertThrows(IllegalArgumentException.class, () -> registry.toDomain(null));
    }
}
