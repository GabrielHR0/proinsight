package com.prosup.proinsight.infrastructure.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Executa uma ação somente depois do commit da transação atual.
 * Sem transação ativa, executa imediatamente.
 */
@Component
public class AfterCommitExecutor {

    public void execute(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
