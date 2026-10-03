package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.request.TesteVo2MaxDto;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxEsteiraIncremental;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TesteVo2MaxMapperRegistryTest {

    private final TesteVo2MaxMapperRegistry registry = new TesteVo2MaxMapperRegistry();

    @Test
    void shouldConvertCooperDtoToTesteVo2MaxCooper() {
        var dto = new TesteVo2MaxDto(Protocolo.COOPER, 3000.0);

        TesteVo2Max result = registry.toDomain(dto);

        assertThat(result).isInstanceOf(TesteVo2MaxCooper.class);
        assertThat(((TesteVo2MaxCooper) result).getDistanciaMetros()).isEqualTo(3000);
    }

    @Test
    void shouldConvertEsteiraIncrementalDtoToTeste() {
        var dto = new TesteVo2MaxDto(Protocolo.ESTEIRA_INCREMENTAL, 12.5);
        dto.setInclinacaoPercent(5.0);

        TesteVo2Max result = registry.toDomain(dto);

        assertThat(result).isInstanceOf(TesteVo2MaxEsteiraIncremental.class);
        var esteira = (TesteVo2MaxEsteiraIncremental) result;
        assertThat(esteira.getVelocidadeKmh()).isEqualTo(12.5);
        assertThat(esteira.getInclinacaoPercent()).isEqualTo(5.0);
    }

    @Test
    void shouldThrowWhenProtocoloNotMapped() {
        var dto = new TesteVo2MaxDto(Protocolo.AVALIACAO_FUNCIONAL_IDOSO, 0);

        assertThrows(IllegalArgumentException.class, () -> registry.toDomain(dto));
    }

    @Test
    void shouldThrowWhenDtoIsNull() {
        assertThrows(IllegalArgumentException.class, () -> registry.toDomain(null));
    }
}
