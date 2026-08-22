package com.prosup.proinsight.domain.model.aquecimento;

/**
 * Interface para aquecimentos polimórficos.
 * Diferente de Teste, aquecimentos NÃO calculam nada.
 * Apenas registram o que aconteceu antes do teste.
 */
public interface Aquecimento {

    String gerarCodigo();

    String getCriterio();

    String getDescricao();
}
