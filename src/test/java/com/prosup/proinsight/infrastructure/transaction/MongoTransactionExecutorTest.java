package com.prosup.proinsight.infrastructure.transaction;

import com.mongodb.MongoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoTransactionExecutorTest {

    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private TransactionStatus status;

    private MongoTransactionExecutor executor;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(status);
        executor = new MongoTransactionExecutor(transactionManager);
    }

    @Test
    @DisplayName("retries a transient transaction error and then succeeds")
    void retriesTransientTransactionError() {
        MongoException transientError = new MongoException("transient");
        transientError.addLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL);
        doThrow(transientError).doNothing().when(transactionManager).commit(status);

        AtomicInteger attempts = new AtomicInteger();
        String result = executor.execute(() -> {
            attempts.incrementAndGet();
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(attempts).hasValue(2);
        verify(transactionManager, times(2)).commit(status);
    }

    @Test
    @DisplayName("does not retry an unknown commit result")
    void doesNotRetryUnknownCommitResult() {
        MongoException unknownResult = new MongoException("unknown");
        unknownResult.addLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL);
        doThrow(unknownResult).when(transactionManager).commit(status);

        AtomicInteger attempts = new AtomicInteger();
        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            return "ok";
        })).isSameAs(unknownResult);

        assertThat(attempts).hasValue(1);
        verify(transactionManager, times(1)).commit(status);
    }

    @Test
    @DisplayName("does not retry a non-transactional failure")
    void doesNotRetryNonTransactionalFailure() {
        doThrow(new IllegalStateException("invalid state")).when(transactionManager).commit(status);

        AtomicInteger attempts = new AtomicInteger();
        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            return "ok";
        })).isInstanceOf(IllegalStateException.class);

        assertThat(attempts).hasValue(1);
    }

    @Test
    @DisplayName("stops after three transient failures")
    void stopsAfterMaxAttempts() {
        MongoException transientError = new MongoException("transient");
        transientError.addLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL);
        doThrow(transientError).when(transactionManager).commit(status);

        AtomicInteger attempts = new AtomicInteger();
        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            return "ok";
        })).isSameAs(transientError);

        assertThat(attempts).hasValue(3);
        verify(transactionManager, times(3)).commit(status);
    }

    @Test
    @DisplayName("detects a transient error inside a wrapped exception")
    void detectsWrappedTransientError() {
        MongoException transientError = new MongoException("transient");
        transientError.addLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL);
        doThrow(new RuntimeException("wrapped", transientError))
                .doNothing()
                .when(transactionManager).commit(status);

        AtomicInteger attempts = new AtomicInteger();
        executor.execute(() -> {
            attempts.incrementAndGet();
            return "ok";
        });

        assertThat(attempts).hasValue(2);
    }
}
