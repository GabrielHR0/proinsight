package com.prosup.proinsight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BackpressureFilterTest {

    private BackpressureProperties properties;
    private BackpressureFilter filter;

    @BeforeEach
    void setUp() {
        properties = new BackpressureProperties();
        properties.setMaxConcurrentRequests(10);
        properties.setQueueCapacity(0);
        properties.setQueueTimeoutMs(100);
        properties.setAuthMaxConcurrentRequests(10);
        properties.setAuthQueueCapacity(0);
        properties.setAuthQueueTimeoutMs(100);
        properties.setRetryAfterSeconds(3);
        filter = new BackpressureFilter(properties, new ObjectMapper());
    }

    @Test
    @DisplayName("deve retornar 503 quando o limite global de concorrencia esta esgotado")
    void rejectsWhenGlobalConcurrencyIsExhausted() throws Exception {
        properties.setMaxConcurrentRequests(1);
        recreateFilter();

        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        Thread first = new Thread(() -> {
            try {
                filter.doFilter(
                        request("GET", "/api/v1/clientes"),
                        new MockHttpServletResponse(),
                        blockingChain(firstEntered, releaseFirst));
            } catch (Throwable t) {
                firstError.set(t);
            }
        });
        first.start();
        assertThat(firstEntered.await(1, TimeUnit.SECONDS)).isTrue();

        var rejected = new MockHttpServletResponse();
        AtomicBoolean rejectedChainCalled = new AtomicBoolean(false);

        filter.doFilter(request("GET", "/api/v1/clientes"), rejected, (req, res) -> {
            rejectedChainCalled.set(true);
        });

        assertThat(rejectedChainCalled).isFalse();
        assertThat(rejected.getStatus()).isEqualTo(503);
        assertThat(rejected.getHeader("Retry-After")).isEqualTo("3");
        assertThat(rejected.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(rejected.getContentType()).startsWith("application/problem+json");
        assertThat(rejected.getContentAsString())
                .contains("\"type\":\"proinsight://problems/service-unavailable\"")
                .contains("\"status\":503")
                .contains("\"instance\":\"/api/v1/clientes\"");

        releaseFirst.countDown();
        first.join(2_000);
        assertThat(first.isAlive()).isFalse();
        assertThat(firstError.get()).isNull();

        var afterRelease = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        filter.doFilter(request("GET", "/api/v1/clientes"), afterRelease, (req, res) -> {
            chainCalled.set(true);
        });

        assertThat(chainCalled).isTrue();
        assertThat(afterRelease.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("deve aplicar limite separado para autenticacao e nao bloquear outras rotas")
    void limitsAuthenticationConcurrencySeparately() throws Exception {
        properties.setAuthMaxConcurrentRequests(1);
        recreateFilter();

        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        Thread first = new Thread(() -> {
            try {
                filter.doFilter(
                        request("POST", "/api/v1/auth/login"),
                        new MockHttpServletResponse(),
                        blockingChain(firstEntered, releaseFirst));
            } catch (Throwable t) {
                firstError.set(t);
            }
        });
        first.start();
        assertThat(firstEntered.await(1, TimeUnit.SECONDS)).isTrue();

        var rejected = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/api/v1/auth/login"), rejected, (req, res) -> {
        });

        assertThat(rejected.getStatus()).isEqualTo(429);
        assertThat(rejected.getContentType()).startsWith("application/problem+json");
        assertThat(rejected.getContentAsString())
                .contains("\"type\":\"proinsight://problems/too-many-requests\"");

        var nonAuthResponse = new MockHttpServletResponse();
        AtomicBoolean nonAuthCalled = new AtomicBoolean(false);
        filter.doFilter(request("GET", "/api/v1/clientes"), nonAuthResponse, (req, res) -> {
            nonAuthCalled.set(true);
        });

        assertThat(nonAuthCalled).isTrue();
        assertThat(nonAuthResponse.getStatus()).isEqualTo(200);

        releaseFirst.countDown();
        first.join(2_000);
        assertThat(first.isAlive()).isFalse();
        assertThat(firstError.get()).isNull();
    }

    @Test
    @DisplayName("deve aguardar na fila limitada antes de rejeitar")
    void waitsInLimitedQueueBeforeRejecting() throws Exception {
        properties.setMaxConcurrentRequests(1);
        properties.setQueueCapacity(1);
        properties.setQueueTimeoutMs(120);
        recreateFilter();

        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        Thread first = new Thread(() -> {
            try {
                filter.doFilter(
                        request("GET", "/api/v1/clientes"),
                        new MockHttpServletResponse(),
                        blockingChain(firstEntered, releaseFirst));
            } catch (Throwable t) {
                firstError.set(t);
            }
        });
        first.start();
        assertThat(firstEntered.await(1, TimeUnit.SECONDS)).isTrue();

        var response = new MockHttpServletResponse();
        long startedAt = System.nanoTime();
        filter.doFilter(request("GET", "/api/v1/clientes"), response, (req, res) -> {
        });
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        assertThat(elapsedMs).isGreaterThanOrEqualTo(80);
        assertThat(response.getStatus()).isEqualTo(503);

        releaseFirst.countDown();
        first.join(2_000);
        assertThat(first.isAlive()).isFalse();
        assertThat(firstError.get()).isNull();
    }

    @Test
    @DisplayName("deve liberar vaga da fila quando a thread e interrompida")
    void releasesQueueSlotWhenInterrupted() throws Exception {
        properties.setMaxConcurrentRequests(1);
        properties.setQueueCapacity(1);
        properties.setQueueTimeoutMs(5_000);
        recreateFilter();

        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        Thread first = new Thread(() -> {
            try {
                filter.doFilter(
                        request("GET", "/api/v1/clientes"),
                        new MockHttpServletResponse(),
                        blockingChain(firstEntered, releaseFirst));
            } catch (Throwable t) {
                firstError.set(t);
            }
        });
        first.start();
        assertThat(firstEntered.await(1, TimeUnit.SECONDS)).isTrue();

        var interruptedResponse = new MockHttpServletResponse();
        AtomicReference<Throwable> secondError = new AtomicReference<>();

        Thread second = new Thread(() -> {
            try {
                filter.doFilter(request("GET", "/api/v1/clientes"), interruptedResponse, (req, res) -> {
                });
            } catch (Throwable t) {
                secondError.set(t);
            }
        });
        second.start();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (second.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertThat(second.getState()).isEqualTo(Thread.State.TIMED_WAITING);

        second.interrupt();
        second.join(2_000);

        assertThat(second.isAlive()).isFalse();
        assertThat(secondError.get()).isNull();
        assertThat(interruptedResponse.getStatus()).isEqualTo(503);

        releaseFirst.countDown();
        first.join(2_000);
        assertThat(first.isAlive()).isFalse();
        assertThat(firstError.get()).isNull();

        var afterInterrupt = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        filter.doFilter(request("GET", "/api/v1/clientes"), afterInterrupt, (req, res) -> {
            chainCalled.set(true);
        });

        assertThat(chainCalled).isTrue();
        assertThat(afterInterrupt.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("deve liberar a vaga quando a cadeia lanca excecao")
    void releasesPermitWhenChainThrows() throws Exception {
        properties.setMaxConcurrentRequests(1);
        recreateFilter();

        assertThatThrownBy(() -> filter.doFilter(
                request("GET", "/api/v1/clientes"),
                new MockHttpServletResponse(),
                (req, res) -> {
                    throw new ServletException("falha simulada");
                }))
                .isInstanceOf(ServletException.class)
                .hasMessage("falha simulada");

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        var response = new MockHttpServletResponse();
        filter.doFilter(request("GET", "/api/v1/clientes"), response, (req, res) -> {
            chainCalled.set(true);
        });

        assertThat(chainCalled).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("deve ignorar actuator, OPTIONS e requisicoes quando desabilitado")
    void bypassesActuatorOptionsAndDisabledMode() throws Exception {
        properties.setMaxConcurrentRequests(1);
        recreateFilter();

        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        Thread first = new Thread(() -> {
            try {
                filter.doFilter(
                        request("GET", "/api/v1/clientes"),
                        new MockHttpServletResponse(),
                        blockingChain(firstEntered, releaseFirst));
            } catch (Throwable t) {
                firstError.set(t);
            }
        });
        first.start();
        assertThat(firstEntered.await(1, TimeUnit.SECONDS)).isTrue();

        assertBypassed(request("GET", "/actuator/health"));
        assertBypassed(request("OPTIONS", "/api/v1/clientes"));

        properties.setEnabled(false);
        assertBypassed(request("GET", "/api/v1/clientes"));

        releaseFirst.countDown();
        first.join(2_000);
        assertThat(first.isAlive()).isFalse();
        assertThat(firstError.get()).isNull();
    }

    private void assertBypassed(MockHttpServletRequest request) throws Exception {
        var response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> {
            chainCalled.set(true);
        });

        assertThat(chainCalled).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private void recreateFilter() {
        filter = new BackpressureFilter(properties, new ObjectMapper());
    }

    private MockHttpServletRequest request(String method, String path) {
        return new MockHttpServletRequest(method, path);
    }

    private FilterChain blockingChain(CountDownLatch entered, CountDownLatch release) {
        return (req, res) -> {
            entered.countDown();
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };
    }
}
