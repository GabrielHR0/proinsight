package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.model.AvaliacaoFisica;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxRockport;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoImcDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoVo2MaxDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoFisicaMapperTest {

    private final AvaliacaoFisicaMapper mapper = new AvaliacaoFisicaMapper();

    @Test
    void shouldRoundTripVo2Max() {
        var now = Instant.now();
        var domain = new AvaliacaoFisica();
        domain.setId("av-1");
        domain.setClienteId("cli-1");
        domain.setAvaliadorId("avl-1");
        domain.setProtocoloId("protocolo-vo2max");

        var teste = new TesteVo2MaxCooper(3000);
        var medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX, now, now, now,
                "observacao",
                List.of(teste)
        );
        medicao.setResultado(45);
        domain.setMedicoes(List.of(medicao));

        AvaliacaoFisicaDocument doc = mapper.toDocument(domain);
        AvaliacaoFisica result = mapper.toDomain(doc);

        assertThat(result.getId()).isEqualTo("av-1");
        assertThat(result.getClienteId()).isEqualTo("cli-1");
        assertThat(result.getAvaliadorId()).isEqualTo("avl-1");
        assertThat(result.getProtocoloId()).isEqualTo("protocolo-vo2max");
        assertThat(result.getMedicoes()).hasSize(1);

        var resultMedicao = result.getMedicoes().get(0);
        assertThat(resultMedicao).isInstanceOf(MedicaoVo2Max.class);
        assertThat(resultMedicao.getTipo()).isEqualTo(MedicaoTipo.VO2_MAX);
        assertThat(resultMedicao.getObservacoes()).isEqualTo("observacao");
    }

    @Test
    void shouldRoundTripImc() {
        var now = Instant.now();
        var domain = new AvaliacaoFisica();
        domain.setId("av-2");
        domain.setClienteId("cli-2");
        domain.setAvaliadorId("avl-2");
        domain.setProtocoloId("protocolo-imc");

        var teste = new TesteImc(85000, 175);
        var medicao = new MedicaoImc(
                MedicaoTipo.IMC, now, now, now,
                "pesagem",
                List.of(teste)
        );
        medicao.setResultado(27.0);
        domain.setMedicoes(List.of(medicao));

        AvaliacaoFisicaDocument doc = mapper.toDocument(domain);
        AvaliacaoFisica result = mapper.toDomain(doc);

        assertThat(result.getId()).isEqualTo("av-2");
        assertThat(result.getClienteId()).isEqualTo("cli-2");
        assertThat(result.getMedicoes()).hasSize(1);

        var resultMedicao = result.getMedicoes().get(0);
        assertThat(resultMedicao).isInstanceOf(MedicaoImc.class);
        assertThat(resultMedicao.getTipo()).isEqualTo(MedicaoTipo.IMC);

        var imc = (MedicaoImc) resultMedicao;
        assertThat(imc.getTeste().getMassaCorporalGramas()).isEqualTo(85000);
        assertThat(imc.getTeste().getAlturaCentimetros()).isEqualTo(175);
    }

    @Test
    void shouldRoundTripVo2MaxWithRockport() {
        var now = Instant.now();
        var domain = new AvaliacaoFisica();
        domain.setId("av-rock");
        domain.setClienteId("cli-rock");
        domain.setProtocoloId("protocolo-rockport");

        var teste = new TesteVo2MaxRockport(15.5, 140, 72.0);
        var medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX, now, now, now,
                "rockport-test",
                List.of(teste)
        );
        medicao.setResultado(38);
        domain.setMedicoes(List.of(medicao));

        AvaliacaoFisicaDocument doc = mapper.toDocument(domain);
        AvaliacaoFisica result = mapper.toDomain(doc);

        var resultMedicao = result.getMedicoes().get(0);
        assertThat(resultMedicao).isInstanceOf(MedicaoVo2Max.class);
        assertThat(resultMedicao.getTipo()).isEqualTo(MedicaoTipo.VO2_MAX);
        assertThat(resultMedicao.getTestes()).hasSize(1);

        var resultTeste = resultMedicao.getTestes().get(0);
        assertThat(resultTeste).isInstanceOf(TesteVo2MaxRockport.class);
        var rockport = (TesteVo2MaxRockport) resultTeste;
        assertThat(rockport.getTempoMinutos()).isEqualTo(15.5);
        assertThat(rockport.getFrequenciaCardiaca()).isEqualTo(140);
    }

    @Test
    void shouldRoundTripRockportViaDocument() {
        var now = Instant.now();
        var medDoc = new MedicaoVo2MaxDocument();
        medDoc.setProtocolo(Protocolo.ROCKPORT);
        medDoc.setTempoSegundos((int) Math.round(15.5 * 60));
        medDoc.setFrequenciaCardiacaBpm(140);
        medDoc.setVo2MaxCalculado(38);
        medDoc.setMedidoEm(now);
        medDoc.setCreatedAt(now);
        medDoc.setUpdatedAt(now);
        medDoc.setObservacoes("rockport-doc");

        var avaliacaoDoc = new AvaliacaoFisicaDocument();
        avaliacaoDoc.setId("av-rock-doc");
        avaliacaoDoc.setClienteId("cli-rock");
        avaliacaoDoc.setMedicoes(List.of(medDoc));

        AvaliacaoFisica domain = mapper.toDomain(avaliacaoDoc);
        AvaliacaoFisicaDocument result = mapper.toDocument(domain);

        assertThat(result.getMedicoes()).hasSize(1);
        var resultDoc = (MedicaoVo2MaxDocument) result.getMedicoes().get(0);
        assertThat(resultDoc.getProtocolo()).isEqualTo(Protocolo.ROCKPORT);
        assertThat(resultDoc.getTempoSegundos()).isEqualTo((int) Math.round(15.5 * 60));
        assertThat(resultDoc.getFrequenciaCardiacaBpm()).isEqualTo(140);
        assertThat(resultDoc.getVo2MaxCalculado()).isEqualTo(38);
    }

    @Test
    void shouldRoundTripVo2MaxViaDocument() {
        var now = Instant.now();
        var medDoc = new MedicaoVo2MaxDocument();
        medDoc.setProtocolo(Protocolo.COOPER);
        medDoc.setDistanciaMetros(3000);
        medDoc.setVo2MaxCalculado(45);
        medDoc.setMedidoEm(now);
        medDoc.setCreatedAt(now);
        medDoc.setUpdatedAt(now);
        medDoc.setObservacoes("teste");

        var avaliacaoDoc = new AvaliacaoFisicaDocument();
        avaliacaoDoc.setId("av-4");
        avaliacaoDoc.setClienteId("cli-4");
        avaliacaoDoc.setMedicoes(List.of(medDoc));

        AvaliacaoFisica domain = mapper.toDomain(avaliacaoDoc);
        AvaliacaoFisicaDocument result = mapper.toDocument(domain);

        assertThat(result.getMedicoes()).hasSize(1);
        var resultDoc = (MedicaoVo2MaxDocument) result.getMedicoes().get(0);
        assertThat(resultDoc.getDistanciaMetros()).isEqualTo(3000);
        assertThat(resultDoc.getProtocolo()).isEqualTo(Protocolo.COOPER);
        assertThat(resultDoc.getVo2MaxCalculado()).isEqualTo(45);
        assertThat(resultDoc.getObservacoes()).isEqualTo("teste");
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertThat(mapper.toDocument(null)).isNull();
        assertThat(mapper.toDomain((AvaliacaoFisicaDocument) null)).isNull();
    }

    @Test
    void shouldHandleEmptyMedicoesList() {
        var domain = new AvaliacaoFisica();
        domain.setId("av-3");
        domain.setClienteId("cli-3");
        domain.setMedicoes(List.of());

        AvaliacaoFisicaDocument doc = mapper.toDocument(domain);
        AvaliacaoFisica result = mapper.toDomain(doc);

        assertThat(result.getMedicoes()).isEmpty();
    }
}
