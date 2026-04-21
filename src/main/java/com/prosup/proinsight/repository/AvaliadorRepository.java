package com.prosup.proinsight.repository;

import com.prosup.proinsight.domain.model.Avaliador;

import java.util.Optional;

/**
 * Domain-level repository interface for Avaliador. Currently left as a thin abstraction
 * and may be implemented by persistence adapters or services directly using Spring Data.
 */
public interface AvaliadorRepository {

    Avaliador save(Avaliador avaliador);

    Optional<Avaliador> findByUserId(String userId);
}

