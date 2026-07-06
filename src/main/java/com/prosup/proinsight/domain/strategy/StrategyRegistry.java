package com.prosup.proinsight.domain.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyRegistry {

    private final Map<String, AvaliacaoStrategy<?>> allStrategies;

    public StrategyRegistry(List<AvaliacaoStrategy<?>> strategies){
        this.allStrategies = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getClass().getAnnotation(StrategyFor.class).value(),
                        Function.identity()
                ));
    }

    @SuppressWarnings("unchecked")
    public <C extends AvaliacaoContext<?, ?>> AvaliacaoStrategy<C> resolve(String key) {
        var strategy = allStrategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("Estratégia não encontrada: " + key);
        }
        return (AvaliacaoStrategy<C>) strategy;
    }
}
