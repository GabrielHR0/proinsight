package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.bootstrap.TabelaClassificacaoInitializer;
import com.prosup.proinsight.config.properties.TabelaClassificacaoProperties;
import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import com.prosup.proinsight.domain.model.ClassificacaoLegivel;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.PercentilFuncional;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaClassificacaoGenerica;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedComponent;
import com.prosup.proinsight.infrastructure.persistence.document.composite.PersistedPercentilFuncional;
import com.prosup.proinsight.infrastructure.persistence.mapper.AvaliacaoResponseMapper;
import com.prosup.proinsight.infrastructure.persistence.mapper.PersistedComponentRegistry;
import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SentarLevantarPercentilTest {

    @Mock
    private TabelaClassificacaoRepository repository;

    private TabelaClassificacaoProperties properties;
    private TabelaClassificacaoInitializer initializer;
    private PersistedComponentRegistry registry;
    private AvaliacaoResponseMapper responseMapper;

    @BeforeEach
    void setUp() {
        properties = new TabelaClassificacaoProperties();
        initializer = new TabelaClassificacaoInitializer(repository, properties);
        registry = new PersistedComponentRegistry();
        ReflectionTestUtils.invokeMethod(registry, "init");
        responseMapper = new AvaliacaoResponseMapper();
    }

    @Test
    @DisplayName("Deve classificar idosa de 62 anos (faixa 60-64) corretamente de acordo com os percentis da tabela")
    void deveClassificarIdosa62AnosFaixa6064() {
        PersistedComponent raizPersistida = ReflectionTestUtils.invokeMethod(initializer, "criarRaizFullertonSentarLevantar");
        var raiz = (TabelaClassificacaoGenerica) registry.toDomain(raizPersistida);

        assertClassificacao(raiz, 62, 22.0, "ELEVADO", 95);
        assertClassificacao(raiz, 62, 21.0, "ELEVADO", 95);
        assertClassificacao(raiz, 62, 20.0, "ELEVADO", 90);
        assertClassificacao(raiz, 62, 19.0, "ELEVADO", 85);
        assertClassificacao(raiz, 62, 18.0, "ELEVADO", 80);
        assertClassificacao(raiz, 62, 17.0, "NORMAL", 75);
        assertClassificacao(raiz, 62, 16.0, "NORMAL", 65);
        assertClassificacao(raiz, 62, 15.0, "NORMAL", 55);
        assertClassificacao(raiz, 62, 14.0, "NORMAL", 45);
        assertClassificacao(raiz, 62, 13.0, "NORMAL", 35);
        assertClassificacao(raiz, 62, 12.0, "NORMAL", 30);
        assertClassificacao(raiz, 62, 11.0, "BAIXO", 20);
        assertClassificacao(raiz, 62, 10.0, "BAIXO", 15);
        assertClassificacao(raiz, 62, 9.0, "BAIXO", 10);
        assertClassificacao(raiz, 62, 8.0, "BAIXO", 5);
        assertClassificacao(raiz, 62, 7.0, "BAIXO", null);
        assertClassificacao(raiz, 62, 0.0, "BAIXO", null);
    }

    @Test
    @DisplayName("Deve classificar idosa de 92 anos (faixa 90-94) com valor 0 no P5")
    void deveClassificarIdosa92AnosFaixa9094() {
        PersistedComponent raizPersistida = ReflectionTestUtils.invokeMethod(initializer, "criarRaizFullertonSentarLevantar");
        var raiz = (TabelaClassificacaoGenerica) registry.toDomain(raizPersistida);

        assertClassificacao(raiz, 92, 16.0, "ELEVADO", 95);
        assertClassificacao(raiz, 92, 15.0, "ELEVADO", 90);
        assertClassificacao(raiz, 92, 13.0, "ELEVADO", 85);
        assertClassificacao(raiz, 92, 0.0, "BAIXO", 5);
    }

    @Test
    @DisplayName("Deve classificar idoso de 68 anos (faixa 65-69 masculino) de acordo com os percentis da tabela")
    void deveClassificarIdoso68AnosFaixa6569Masculino() {
        PersistedComponent raizPersistida = ReflectionTestUtils.invokeMethod(initializer, "criarRaizFullertonSentarLevantar");
        var raiz = (TabelaClassificacaoGenerica) registry.toDomain(raizPersistida);

        assertClassificacao(raiz, 68, Sexo.MASCULINO, 24.0, "ELEVADO", 95);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 23.0, "ELEVADO", 95);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 21.0, "ELEVADO", 90);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 20.0, "ELEVADO", 85);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 19.0, "ELEVADO", 80);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 18.0, "NORMAL", 75);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 17.0, "NORMAL", 65);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 16.0, "NORMAL", 60);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 15.0, "NORMAL", 50);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 14.0, "NORMAL", 40);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 13.0, "NORMAL", 35);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 12.0, "NORMAL", 25);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 11.0, "BAIXO", 20);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 9.0, "BAIXO", 10);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 8.0, "BAIXO", 5);
        assertClassificacao(raiz, 68, Sexo.MASCULINO, 5.0, "BAIXO", null);
    }

    @Test
    @DisplayName("Deve converter PercentilFuncional entre Domain e Persisted sem perda de dados")
    void deveFazerRoundtripDomainPersisted() {
        var domain = new PercentilFuncional(95, 21.0, null);
        var persisted = (PersistedPercentilFuncional) registry.toPersisted(domain);

        assertThat(persisted.getPercentil()).isEqualTo(95);
        assertThat(persisted.getMin()).isEqualTo(21.0);
        assertThat(persisted.getMax()).isNull();

        var deVolta = (PercentilFuncional) registry.toDomain(persisted);
        assertThat(deVolta.getPercentil()).isEqualTo(95);
        assertThat(deVolta.getMin()).isEqualTo(21.0);
    }

    private void assertClassificacao(
        TabelaClassificacaoGenerica raiz,
        int idade,
        double valor,
        String esperadoClassificacao,
        Integer esperadoPercentil
    ) {
        assertClassificacao(raiz, idade, Sexo.FEMININO, valor, esperadoClassificacao, esperadoPercentil);
    }

    private void assertClassificacao(
        TabelaClassificacaoGenerica raiz,
        int idade,
        Sexo sexo,
        double valor,
        String esperadoClassificacao,
        Integer esperadoPercentil
    ) {
        var dados = new DadosAvaliacao()
            .adicionar("idade", idade)
            .adicionar("sexo", sexo);
        var teste = new SentarLevantar(TesteFuncionalTipo.SENTAR_LEVANTAR_30S, valor, (int) valor);

        Leaf leaf = raiz.classificarComTeste(teste, dados);
        assertThat(leaf).isNotNull();
        assertThat(leaf).isInstanceOf(PercentilFuncional.class);

        var p = (PercentilFuncional) leaf;
        assertThat(p.getPercentil()).as("Percentil numérico para valor " + valor).isEqualTo(esperadoPercentil);

        String nomeClassificacao = responseMapper.obterNomeClassificacao(leaf);
        assertThat(nomeClassificacao).as("Classificação para valor " + valor).isEqualTo(esperadoClassificacao);
    }
}
