package com.prosup.proinsight.domain.strategy;

import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyRegistry {

    private final Map<String, AvaliacaoStrategy<?>> allStrategies;
    private final Map<String, Class<?>> contextTypes;

    public StrategyRegistry(List<AvaliacaoStrategy<?>> strategies){
        this.allStrategies = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getClass().getAnnotation(StrategyFor.class).value(),
                        Function.identity()
                ));
        this.contextTypes = this.allStrategies.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> obterTipoContexto(e.getValue())
                ));
    }

    public <C extends AvaliacaoContext<?, ?>> AvaliacaoStrategy<C> resolve(String key) {
        return resolve(key, null);
    }

    public <C extends AvaliacaoContext<?, ?>> AvaliacaoStrategy<C> resolve(String key, Class<C> contextTypeEsperado) {
        var strategy = allStrategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("Estratégia não encontrada: " + key);
        }
        if (contextTypeEsperado != null) {
            Class<?> tipoSuportado = contextTypes.get(key);
            if (!tipoSuportado.isAssignableFrom(contextTypeEsperado)) {
                throw new IllegalStateException(
                    "Estratégia '" + key + "' (" + strategy.getClass().getSimpleName()
                        + ") espera contexto " + tipoSuportado.getSimpleName()
                        + ", mas foi solicitado " + contextTypeEsperado.getSimpleName()
                        + ". Verifique o strategyKey do protocolo persistido."
                );
            }
        }
        @SuppressWarnings("unchecked")
        AvaliacaoStrategy<C> typed = (AvaliacaoStrategy<C>) strategy;
        return typed;
    }

    private static Class<?> obterTipoContexto(AvaliacaoStrategy<?> strategy) {
        for (Class<?> c = strategy.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Type iface : c.getGenericInterfaces()) {
                if (iface instanceof ParameterizedType pt && pt.getRawType().equals(AvaliacaoStrategy.class)) {
                    Type arg = pt.getActualTypeArguments()[0];
                    if (arg instanceof Class<?> classe && AvaliacaoContext.class.isAssignableFrom(classe)) {
                        return classe;
                    }
                }
            }
        }
        throw new IllegalStateException(
            "Strategy " + strategy.getClass().getSimpleName()
                + " não declara o tipo de contexto em AvaliacaoStrategy<...>"
        );
    }
}