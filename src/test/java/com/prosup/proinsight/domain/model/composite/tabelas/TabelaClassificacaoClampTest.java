package com.prosup.proinsight.domain.model.composite.tabelas;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.Protocolo;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.enums.TipoLimite;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TabelaClassificacaoClampTest {

    @Test
    void valorAbaixoDeTodasAsFaixasClassificaNivelMaisProximo() {
        var faixa = criarFaixa(20, 29);
        var dados = new DadosAvaliacao().adicionar("idade", 22);

        Leaf resultado = faixa.classificarComTeste(testeComValor(5.0), dados);

        assertThat(((NivelVo2Max) resultado).getClassificacao()).isEqualTo("RUIM");
    }

    @Test
    void valorAcimaDeTodasAsFaixasClassificaNivelMaisProximo() {
        var faixa = criarFaixa(20, 29);
        var dados = new DadosAvaliacao().adicionar("idade", 22);

        Leaf resultado = faixa.classificarComTeste(testeComValor(45.0), dados);

        assertThat(((NivelVo2Max) resultado).getClassificacao()).isEqualTo("BOM");
    }

    @Test
    void valorDentroDeFaixaClassificaNormalmente() {
        var faixa = criarFaixa(20, 29);
        var dados = new DadosAvaliacao().adicionar("idade", 22);

        Leaf resultado = faixa.classificarComTeste(testeComValor(25.0), dados);

        assertThat(((NivelVo2Max) resultado).getClassificacao()).isEqualTo("MEDIO");
    }

    @Test
    void idadeForaDaFaixaUsaFaixaDeIdadeMaisProxima() {
        var tabelaSexo = new TabelaSexo(Sexo.MASCULINO);
        // Faixa 20-29 com níveis padrão: RUIM [10,20), MEDIO [20,30), BOM [30,40)
        tabelaSexo.add(criarFaixa(20, 29));
        // Faixa 40-49 com níveis deslocados: RUIM [25,35), MEDIO [35,45), BOM [45,55)
        var faixaIdosa = new TabelaIdade(40, 49);
        faixaIdosa.add(new NivelVo2Max("RUIM", 25.0, 35.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixaIdosa.add(new NivelVo2Max("MEDIO", 35.0, 45.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixaIdosa.add(new NivelVo2Max("BOM", 45.0, 55.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        tabelaSexo.add(faixaIdosa);

        // idade 15 -> faixa 20-29 (mais próxima, distância 5); valor 25 -> MEDIO.
        // Se erradamente usasse a faixa 40-49, 25 cairia em RUIM [25,35).
        var dadosJovem = new DadosAvaliacao()
                .adicionar("idade", 15)
                .adicionar("sexo", Sexo.MASCULINO);
        Leaf resultadoJovem = tabelaSexo.classificarComTeste(testeComValor(25.0), dadosJovem);
        assertThat(((NivelVo2Max) resultadoJovem).getClassificacao()).isEqualTo("MEDIO");

        // idade 60 -> faixa 40-49 (mais próxima, distância 11); valor 30 -> RUIM.
        // Se erradamente usasse a faixa 20-29, 30 cairia em BOM [30,40).
        var dadosIdoso = new DadosAvaliacao()
                .adicionar("idade", 60)
                .adicionar("sexo", Sexo.MASCULINO);
        Leaf resultadoIdoso = tabelaSexo.classificarComTeste(testeComValor(30.0), dadosIdoso);
        assertThat(((NivelVo2Max) resultadoIdoso).getClassificacao()).isEqualTo("RUIM");
    }

    @Test
    void idadeAusenteUsaPrimeiraFaixa() {
        var tabelaSexo = new TabelaSexo(Sexo.MASCULINO);
        tabelaSexo.add(criarFaixa(20, 29));
        tabelaSexo.add(criarFaixa(40, 49));

        var dados = new DadosAvaliacao().adicionar("sexo", Sexo.MASCULINO);

        Leaf resultado = tabelaSexo.classificarComTeste(testeComValor(15.0), dados);

        // valor 15 na faixa 20-29 -> RUIM (match); na faixa 40-49 -> clamp BOM.
        assertThat(((NivelVo2Max) resultado).getClassificacao()).isEqualTo("RUIM");
    }

    @Test
    void raizClassificaComClampDeIdadeEValor() {
        var raiz = new TabelaClassificacaoGenerica();
        raiz.add(criarSexo(Sexo.MASCULINO));
        raiz.add(criarSexo(Sexo.FEMININO));

        var dados = new DadosAvaliacao()
                .adicionar("idade", 12)
                .adicionar("sexo", Sexo.MASCULINO);

        // idade 12 -> faixa 20-29 (mais próxima); valor 5 -> clamp -> RUIM
        Leaf resultado = raiz.classificarComTeste(testeComValor(5.0), dados);

        assertThat(((NivelVo2Max) resultado).getClassificacao()).isEqualTo("RUIM");
    }

    private TabelaSexo criarSexo(Sexo sexo) {
        var tabelaSexo = new TabelaSexo(sexo);
        tabelaSexo.add(criarFaixa(20, 29));
        tabelaSexo.add(criarFaixa(40, 49));
        return tabelaSexo;
    }

    /** Faixa com níveis totalmente fechados (sem extremos abertos) para forçar o clamp. */
    private TabelaIdade criarFaixa(int idadeMin, int idadeMax) {
        var faixa = new TabelaIdade(idadeMin, idadeMax);
        faixa.add(new NivelVo2Max("RUIM", 10.0, 20.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("MEDIO", 20.0, 30.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        faixa.add(new NivelVo2Max("BOM", 30.0, 40.0, TipoLimite.INCLUSIVO, TipoLimite.EXCLUSIVO));
        return faixa;
    }

    private TesteVo2Max testeComValor(double valor) {
        return new TesteVo2Max(Protocolo.COOPER, valor) {
            @Override
            public Double calcularVo2Max(DadosAvaliacao dados) {
                return valor;
            }

            @Override
            public String gerarCodigo() {
                return "teste-clamp";
            }
        };
    }
}
