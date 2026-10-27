package com.prosup.proinsight.infrastructure.transaction;

import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Executa um bloco dentro de uma transação MongoDB, repetindo apenas falhas
 * transitórias do servidor (TransientTransactionError).
 *
 * O retry do commit com resultado desconhecido não é feito aqui de propósito:
 * repetir a transação inteira nesse cenário poderia duplicar efeitos já
 * confirmados. Esse caso deve ser tratado no caso de uso específico.
 */
@Component
public class MongoTransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(MongoTransactionExecutor.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 50L;

    private final TransactionTemplate transactionTemplate;

    public MongoTransactionExecutor(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public <T> T execute(Supplier<T> action) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> action.get());
            } catch (RuntimeException ex) {
                if (attempt == MAX_ATTEMPTS || !isTransientTransactionError(ex)) {
                    throw ex;
                }
                log.warn("Transação MongoDB abortada por erro transitório; tentando novamente ({}/{})",
                        attempt + 1, MAX_ATTEMPTS);
                sleepBeforeRetry(attempt);
            }
        }
        throw new IllegalStateException("Número máximo de tentativas de transação excedido");
    }

    private static boolean isTransientTransactionError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof MongoException mongoException
                    && mongoException.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(BASE_BACKOFF_MS * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompido durante retry de transação", ex);
        }
    }
}
