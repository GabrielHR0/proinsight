package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.api.dto.request.MedicaoVo2MaxDto;
import com.prosup.proinsight.api.dto.request.TesteVo2MaxDto;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxRockport;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoVo2MaxDtoMapperTest {

    private final TesteVo2MaxMapperRegistry testeRegistry = new TesteVo2MaxMapperRegistry();
    private final AvaliacaoVo2MaxDtoMapper mapper = new AvaliacaoVo2MaxDtoMapper(testeRegistry);

    @Test
    void shouldConvertMedicaoDtoWithCooperTest() {
        var testeDto = new TesteVo2MaxDto(Protocolo.COOPER, 3000.0);
        var medicaoDto = new MedicaoVo2MaxDto(
                null, "observacao",
                List.of(testeDto)
        );

        MedicaoVo2Max result = mapper.toMedicaoDomain(medicaoDto);

        assertThat(result).isNotNull();
        assertThat(result.getTipo()).isEqualTo(MedicaoTipo.VO2_MAX);
        assertThat(result.getObservacoes()).isEqualTo("observacao");
        assertThat(result.getTestes()).hasSize(1);
        assertThat(result.getTestes().get(0)).isInstanceOf(TesteVo2MaxCooper.class);
        assertThat(((TesteVo2MaxCooper) result.getTestes().get(0)).getDistanciaMetros()).isEqualTo(3000);
    }

    @Test
    void shouldConvertMedicaoDtoWithRockportTest() {
        var testeDto = new TesteVo2MaxDto(Protocolo.ROCKPORT, 12.5);
        testeDto.setFrequenciaCardiaca(145);
        testeDto.setPesoKg(70.0);
        var medicaoDto = new MedicaoVo2MaxDto(
                null, null,
                List.of(testeDto)
        );

        MedicaoVo2Max result = mapper.toMedicaoDomain(medicaoDto);

        assertThat(result.getTestes()).hasSize(1);
        assertThat(result.getTestes().get(0)).isInstanceOf(TesteVo2MaxRockport.class);
        var rockport = (TesteVo2MaxRockport) result.getTestes().get(0);
        assertThat(rockport.getTempoMinutos()).isEqualTo(12.5);
        assertThat(rockport.getFrequenciaCardiaca()).isEqualTo(145);
        assertThat(rockport.getPesoKg()).isEqualTo(70.0);
    }

    @Test
    void shouldConvertMedicaoDtoWithMultipleTests() {
        var cooper = new TesteVo2MaxDto(Protocolo.COOPER, 3000.0);
        var rockport = new TesteVo2MaxDto(Protocolo.ROCKPORT, 15.0);
        rockport.setFrequenciaCardiaca(140);
        rockport.setPesoKg(75.0);
        var medicaoDto = new MedicaoVo2MaxDto(
                null, null,
                List.of(cooper, rockport)
        );

        MedicaoVo2Max result = mapper.toMedicaoDomain(medicaoDto);

        assertThat(result.getTestes()).hasSize(2);
        assertThat(result.getTestes().get(0)).isInstanceOf(TesteVo2MaxCooper.class);
        assertThat(result.getTestes().get(1)).isInstanceOf(TesteVo2MaxRockport.class);
    }

    @Test
    void shouldUseMedidoEmWhenProvided() {
        var medidoEm = Instant.parse("2025-06-15T10:30:00Z");
        var testeDto = new TesteVo2MaxDto(Protocolo.COOPER, 3000.0);
        var medicaoDto = new MedicaoVo2MaxDto(
                medidoEm, null,
                List.of(testeDto)
        );

        MedicaoVo2Max result = mapper.toMedicaoDomain(medicaoDto);

        assertThat(result.getMedidoEm()).isEqualTo(medidoEm);
    }

    @Test
    void shouldUseNowWhenMedidoEmIsNull() {
        var testeDto = new TesteVo2MaxDto(Protocolo.COOPER, 3000.0);
        var medicaoDto = new MedicaoVo2MaxDto(
                null, null,
                List.of(testeDto)
        );

        MedicaoVo2Max result = mapper.toMedicaoDomain(medicaoDto);
        long diff = Instant.now().getEpochSecond() - result.getMedidoEm().getEpochSecond();

        assertThat(diff).isLessThan(5);
    }

    @Test
    void shouldReturnNullWhenDtoIsNull() {
        assertThat(mapper.toMedicaoDomain(null)).isNull();
    }
}
