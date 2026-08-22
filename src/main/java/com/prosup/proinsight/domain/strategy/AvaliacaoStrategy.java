package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.domain.model.composite.Leaf;


public interface AvaliacaoStrategy<C extends AvaliacaoContext<?, ?>> {

    Leaf avaliar(C contexto);
}
