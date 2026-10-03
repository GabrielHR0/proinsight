package com.prosup.proinsight.infrastructure.persistence.mapper;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import com.prosup.proinsight.domain.model.AvaliacaoFisica;
import com.prosup.proinsight.domain.model.MedicaoFuncional;
import com.prosup.proinsight.domain.model.MedicaoImc;
import com.prosup.proinsight.domain.model.MedicaoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteImc;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxCooper;
import com.prosup.proinsight.domain.model.teste.TesteVo2MaxEsteiraIncremental;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.AlcancarCostas;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.LevantarCaminhar2m5;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.MarchaEstacionaria;
import com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos.SentarLevantar;
import com.prosup.proinsight.infrastructure.persistence.document.AvaliacaoFisicaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoFuncionalDocument;
import com.prosup.proinsight.infrastructure.persistence.document.MedicaoVo2MaxDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
    void shouldRoundTripVo2MaxWithEsteiraIncremental() {
        var now = Instant.now();
        var domain = new AvaliacaoFisica();
        domain.setId("av-esteira");
        domain.setClienteId("cli-esteira");
        domain.setProtocoloId("protocolo-esteira-incremental");

        var teste = new TesteVo2MaxEsteiraIncremental(12.5, 5.0);
        var medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX, now, now, now,
                "esteira-test",
                List.of(teste)
        );
        medicao.setResultado(42);
        domain.setMedicoes(List.of(medicao));

        AvaliacaoFisicaDocument doc = mapper.toDocument(domain);
        AvaliacaoFisica result = mapper.toDomain(doc);

        var resultMedicao = result.getMedicoes().get(0);
        assertThat(resultMedicao).isInstanceOf(MedicaoVo2Max.class);
        assertThat(resultMedicao.getTipo()).isEqualTo(MedicaoTipo.VO2_MAX);
        assertThat(resultMedicao.getTestes()).hasSize(1);

        var resultTeste = resultMedicao.getTestes().get(0);
        assertThat(resultTeste).isInstanceOf(TesteVo2MaxEsteiraIncremental.class);
        var esteira = (TesteVo2MaxEsteiraIncremental) resultTeste;
        assertThat(esteira.getVelocidadeKmh()).isEqualTo(12.5);
        assertThat(esteira.getInclinacaoPercent()).isEqualTo(5.0);
    }

    @Test
    void shouldThrowWhenProtocoloIsNotSupportedOnRead() {
        var now = Instant.now();
        var medDoc = new MedicaoVo2MaxDocument();
        medDoc.setProtocolo(Protocolo.IMC);
        medDoc.setVo2MaxCalculado(30);
        medDoc.setMedidoEm(now);
        medDoc.setCreatedAt(now);
        medDoc.setUpdatedAt(now);

        var avaliacaoDoc = new AvaliacaoFisicaDocument();
        avaliacaoDoc.setId("av-unsupported");
        avaliacaoDoc.setClienteId("cli-unsupported");
        avaliacaoDoc.setMedicoes(List.of(medDoc));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDomain(avaliacaoDoc)
        );
    }

    @Test
    void shouldThrowWhenProtocoloIsNullOnRead() {
        var now = Instant.now();
        var medDoc = new MedicaoVo2MaxDocument();
        medDoc.setVo2MaxCalculado(30);
        medDoc.setMedidoEm(now);
        medDoc.setCreatedAt(now);
        medDoc.setUpdatedAt(now);

        var avaliacaoDoc = new AvaliacaoFisicaDocument();
        avaliacaoDoc.setId("av-null-protocolo");
        avaliacaoDoc.setClienteId("cli-null-protocolo");
        avaliacaoDoc.setMedicoes(List.of(medDoc));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDomain(avaliacaoDoc)
        );
    }

    @Test
    void shouldThrowWhenTesteVo2MaxIsNotSupportedOnWrite() {
        var now = Instant.now();
        var domain = new AvaliacaoFisica();
        domain.setId("av-write-unsupported");
        domain.setClienteId("cli-write");

        var testeNulo = new com.prosup.proinsight.domain.model.teste.TesteVo2Max(
                Protocolo.IMC, null) {
            @Override
            public Double calcularVo2Max(com.prosup.proinsight.domain.DadosAvaliacao dados) {
                return null;
            }

            @Override
            public String gerarCodigo() {
                return "TESTE_IMC";
            }
        };
        var medicao = new MedicaoVo2Max(
                MedicaoTipo.VO2_MAX, now, now, now,
                "sem-suporte",
                List.of(testeNulo)
        );
        medicao.setResultado(30);
        domain.setMedicoes(List.of(medicao));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDocument(domain)
        );
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
    void shouldRoundTripFuncional() {
        var now = Instant.now();
        var domain = new AvaliacaoFisica();
        domain.setId("av-funcional");
        domain.setClienteId("cli-funcional");
        domain.setProtocoloId("protocolo_avaliacao_funcional_idoso");

        var medicao = new MedicaoFuncional(
                MedicaoTipo.FUNCIONAL, now, now, now,
                "bateria fullerton",
                List.of(
                    new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, 14.0),
                    new AlcancarCostas(TesteFuncionalTipo.ALCANCAR_COSTAS, -3.5),
                    new LevantarCaminhar2m5(TesteFuncionalTipo.LEVANTAR_CAMINHAR_2M5, 8.5)
                )
        );
        medicao.setClassificacoes(Map.of(
                "SENTAR_LEVANTAR_30S", "BOM",
                "ALCANCAR_COSTAS", "RUIM",
                "LEVANTAR_CAMINHAR_2M5", "MÉDIO"
        ));
        domain.setMedicoes(List.of(medicao));

        AvaliacaoFisicaDocument doc = mapper.toDocument(domain);
        AvaliacaoFisica result = mapper.toDomain(doc);

        assertThat(result.getMedicoes()).hasSize(1);

        var resultMedicao = result.getMedicoes().get(0);
        assertThat(resultMedicao).isInstanceOf(MedicaoFuncional.class);
        assertThat(resultMedicao.getTipo()).isEqualTo(MedicaoTipo.FUNCIONAL);
        assertThat(resultMedicao.getObservacoes()).isEqualTo("bateria fullerton");

        var funcional = (MedicaoFuncional) resultMedicao;
        assertThat(funcional.getTestes()).hasSize(3);
        assertThat(funcional.getTestes().get(0).getTipo()).isEqualTo(TesteFuncionalTipo.SENTAR_LEVANTAR_30S);
        assertThat(funcional.getTestes().get(0).getValor()).isEqualTo(14.0);
        assertThat(funcional.getTestes().get(1).getTipo()).isEqualTo(TesteFuncionalTipo.ALCANCAR_COSTAS);
        assertThat(funcional.getTestes().get(1).getValor()).isEqualTo(-3.5);
        assertThat(funcional.getTestes().get(2).getTipo()).isEqualTo(TesteFuncionalTipo.LEVANTAR_CAMINHAR_2M5);
        assertThat(funcional.getTestes().get(2).getValor()).isEqualTo(8.5);
        assertThat(funcional.getClassificacoes())
                .containsEntry("SENTAR_LEVANTAR_30S", "BOM")
                .containsEntry("ALCANCAR_COSTAS", "RUIM")
                .containsEntry("LEVANTAR_CAMINHAR_2M5", "MÉDIO");
    }

    @Test
    void shouldBuildFuncionalAvaliacaoDocument() {
        var now = Instant.now();
        var medicao = new MedicaoFuncional(
                MedicaoTipo.FUNCIONAL, now, now, now,
                "apenas um teste",
                List.of(new MarchaEstacionaria(TesteFuncionalTipo.MARCHA_ESTACIONARIA_2MIN, 162.0))
        );
        medicao.setClassificacoes(Map.of("MARCHA_ESTACIONARIA_2MIN", "EXCELENTE"));

        AvaliacaoFisicaDocument doc = mapper.toFuncionalDocument(
                "cli-1", "avl-1", "protocolo_avaliacao_funcional_idoso", medicao);

        assertThat(doc.getClienteId()).isEqualTo("cli-1");
        assertThat(doc.getAvaliadorId()).isEqualTo("avl-1");
        assertThat(doc.getProtocoloId()).isEqualTo("protocolo_avaliacao_funcional_idoso");
        assertThat(doc.getMedicoes()).hasSize(1);

        var medicaoDoc = (MedicaoFuncionalDocument) doc.getMedicoes().get(0);
        assertThat(medicaoDoc.getTipo()).isEqualTo(MedicaoTipo.FUNCIONAL);
        assertThat(medicaoDoc.getMarchaEstacionaria2Min()).isEqualTo(162.0);
        assertThat(medicaoDoc.getSentarLevantar30s()).isNull();
        assertThat(medicaoDoc.getClassificacoes()).containsEntry("MARCHA_ESTACIONARIA_2MIN", "EXCELENTE");
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
