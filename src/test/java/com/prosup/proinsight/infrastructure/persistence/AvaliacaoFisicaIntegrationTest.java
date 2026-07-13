package com.prosup.proinsight.infrastructure.persistence;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.model.AvaliacaoFisica;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxRockport;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoFisicaMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AvaliacaoFisicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoFisicaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AvaliacaoFisicaRepository repository;

    @Autowired
    private AvaliacaoFisicaMapper mapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldPersistAndRetrieveAvaliacaoFisicaWithVo2Max() {
        var teste = new TesteVo2MaxCooper(3000);
        var medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX,
                Instant.parse("2025-01-15T10:00:00Z"),
                Instant.now(),
                Instant.now(),
                "teste de integracao",
                List.of(teste)
        );

        var avaliacaoDomain = new AvaliacaoFisica();
        avaliacaoDomain.setClienteId("cliente-1");
        avaliacaoDomain.setAvaliadorId("avaliador-1");
        avaliacaoDomain.setProtocoloId("protocolo-vo2max");
        avaliacaoDomain.setMedicoes(List.of(medicao));

        AvaliacaoFisicaDocument doc = mapper.toDocument(avaliacaoDomain);
        AvaliacaoFisicaDocument saved = repository.save(doc);

        assertThat(saved.getId()).isNotNull();

        AvaliacaoFisicaDocument loaded = repository.findById(saved.getId()).orElseThrow();
        AvaliacaoFisica result = mapper.toDomain(loaded);

        assertThat(result.getClienteId()).isEqualTo("cliente-1");
        assertThat(result.getAvaliadorId()).isEqualTo("avaliador-1");
        assertThat(result.getProtocoloId()).isEqualTo("protocolo-vo2max");
        assertThat(result.getMedicoes()).hasSize(1);

        var resultMedicao = result.getMedicoes().get(0);
        assertThat(resultMedicao).isInstanceOf(MedicaoVo2Max.class);
        assertThat(resultMedicao.getTipo()).isEqualTo(MedicaoTipo.VO2_MAX);
        assertThat(resultMedicao.getTestes()).hasSize(1);
        assertThat(resultMedicao.getTestes().get(0)).isInstanceOf(TesteVo2MaxCooper.class);

        var testeResult = (TesteVo2MaxCooper) resultMedicao.getTestes().get(0);
        assertThat(testeResult.getDistanciaMetros()).isEqualTo(3000);
    }

    @Test
    void shouldPersistAndRetrieveAvaliacaoFisicaWithRockport() {
        var teste = new TesteVo2MaxRockport(15.5, 140, 72.0);
        var medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                null,
                List.of(teste)
        );

        var avaliacaoDomain = new AvaliacaoFisica();
        avaliacaoDomain.setClienteId("cliente-rock");
        avaliacaoDomain.setProtocoloId("protocolo-rockport");
        avaliacaoDomain.setMedicoes(List.of(medicao));

        AvaliacaoFisicaDocument saved = repository.save(mapper.toDocument(avaliacaoDomain));
        AvaliacaoFisica result = mapper.toDomain(repository.findById(saved.getId()).orElseThrow());

        assertThat(result.getMedicoes()).hasSize(1);
        assertThat(result.getMedicoes().get(0).getTestes().get(0))
                .isInstanceOf(TesteVo2MaxRockport.class);
    }

    @Test
    void shouldPersistAndRetrieveAvaliacaoFisicaWithMultipleMedicoes() {
        var teste = new TesteVo2MaxCooper(2400);
        var medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                null,
                List.of(teste)
        );

        var avaliacaoDomain = new AvaliacaoFisica();
        avaliacaoDomain.setClienteId("cliente-multi");
        avaliacaoDomain.setProtocoloId("protocolo-vo2max");
        avaliacaoDomain.setMedicoes(List.of(medicao));

        AvaliacaoFisicaDocument saved = repository.save(mapper.toDocument(avaliacaoDomain));
        AvaliacaoFisica result = mapper.toDomain(repository.findById(saved.getId()).orElseThrow());

        assertThat(result.getMedicoes()).hasSize(1);
        assertThat(
            ((TesteVo2MaxCooper) result.getMedicoes().get(0).getTestes().get(0)).getDistanciaMetros()
        ).isEqualTo(2400);
    }

}
