package com.prosup.proinsight.domain.model.teste.aptidao_fisica_idosos;

import com.prosup.proinsight.domain.enums.TesteFuncionalTipo;
import com.prosup.proinsight.domain.model.teste.Teste;

public abstract class TesteAptdaoFisicaIdosos implements Teste {
    protected final String codigo = this.gerarCodigo();
    protected final TesteFuncionalTipo tipo;
    protected Double valor;

    public TesteAptdaoFisicaIdosos(TesteFuncionalTipo tipo) {
        this.tipo = tipo;
    }

    public TesteAptdaoFisicaIdosos(TesteFuncionalTipo tipo, Double valor) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do teste funcional é obrigatório");
        }
        if (valor == null || valor.isNaN() || valor.isInfinite()) {
            throw new IllegalArgumentException(
                    "Valor do teste " + tipo.name() + " deve ser um número válido");
        }
        this.tipo = tipo;
        this.valor = valor;
    }

    protected Double idadeNormalizada(Integer idade, Integer idadeMin, Integer idadeMax){
        Double normalizacao = (double) ((idade - idadeMin) / ((idadeMax - idadeMin) + 1));
        return normalizacao;
    }

    @Override
    public String gerarCodigo() {
        return "FUNC-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    public String getValorClassificacao() {
        return valor != null ? String.valueOf(valor) : null;
    }

    public String getCodigo() {
        return codigo;
    }

    public TesteFuncionalTipo getTipo() {
        return tipo;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
