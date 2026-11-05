package com.prosup.proinsight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * HTTP admission control: limits global concurrency and, separately, the
 * concurrency of endpoints that use BCrypt.
 *
 * <p>Without this filter, Tomcat accepts up to 200 concurrent requests and all
 * of them compete for threads, CPU and MongoDB connections. With the limit,
 * excess requests wait in a short queue; when the queue is also full, the
 * request is rejected immediately with 503/429 and {@code Retry-After}.</p>
 */
public class BackpressureFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BackpressureFilter.class);

    private static final Set<String> AUTH_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/me/password"
    );

    private final BackpressureProperties properties;
    private final ObjectMapper objectMapper;
    private final Semaphore globalActive;
    private final Semaphore globalQueue;
    private final Semaphore authActive;
    private final Semaphore authQueue;

    public BackpressureFilter(BackpressureProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.globalActive = new Semaphore(properties.getMaxConcurrentRequests(), true);
        this.globalQueue = new Semaphore(properties.getQueueCapacity(), true);
        this.authActive = new Semaphore(properties.getAuthMaxConcurrentRequests(), true);
        this.authQueue = new Semaphore(properties.getAuthQueueCapacity(), true);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !properties.isEnabled()
                || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        boolean authRequest = AUTH_PATHS.contains(request.getRequestURI());

        Semaphore authPermit = null;
        Semaphore globalPermit = null;
        try {
            if (authRequest) {
                authPermit = acquire(
                        authActive,
                        authQueue,
                        properties.getAuthQueueCapacity(),
                        properties.getAuthQueueTimeoutMs());
                if (authPermit == null) {
                    reject(response, request, 429,
                            "too-many-requests",
                            "Muitas requisições de autenticação simultâneas",
                            "Aguarde antes de tentar novamente.");
                    return;
                }
            }

            globalPermit = acquire(
                    globalActive,
                    globalQueue,
                    properties.getQueueCapacity(),
                    properties.getQueueTimeoutMs());
            if (globalPermit == null) {
                reject(response, request, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "service-unavailable",
                        "Capacidade temporariamente esgotada",
                        "O servidor está processando o máximo de requisições simultâneas.");
                return;
            }

            filterChain.doFilter(request, response);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            reject(response, request, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "service-unavailable",
                    "Requisição interrompida",
                    "A requisição foi interrompida antes de ser processada.");
        } finally {
            release(globalPermit);
            release(authPermit);
        }
    }

    /**
     * Tries to reserve an execution slot. If no slot is immediately available,
     * uses a bounded queue slot and waits at most {@code timeoutMs}.
     *
     * <p>Returns {@code null} when there is no slot or when the queue is full.
     * In that case the request must be rejected without waiting.</p>
     */
    private Semaphore acquire(Semaphore active,
                              Semaphore queue,
                              int queueCapacity,
                              long timeoutMs) throws InterruptedException {
        if (queueCapacity <= 0) {
            return active.tryAcquire() ? active : null;
        }

        if (!queue.tryAcquire()) {
            return null;
        }

        try {
            boolean acquired = timeoutMs > 0
                    ? active.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)
                    : active.tryAcquire();
            return acquired ? active : null;
        } finally {
            queue.release();
        }
    }

    private void release(Semaphore permit) {
        if (permit != null) {
            permit.release();
        }
    }

    private void reject(HttpServletResponse response,
                        HttpServletRequest request,
                        int status,
                        String type,
                        String title,
                        String detail) throws IOException {
        response.setStatus(status);
        response.setHeader("Retry-After", String.valueOf(properties.getRetryAfterSeconds()));
        response.setHeader("Cache-Control", "no-store");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("type", "proinsight://problems/" + type);
        problem.put("title", title);
        problem.put("status", status);
        problem.put("detail", detail);
        problem.put("instance", request.getRequestURI());
        problem.put("timestamp", Instant.now().toString());

        objectMapper.writeValue(response.getWriter(), problem);

        log.warn("Backpressure rejected request: status={}, method={}, path={}, " +
                        "globalActive={}, globalQueued={}, authActive={}, authQueued={}",
                status,
                request.getMethod(),
                request.getRequestURI(),
                activeCount(globalActive, properties.getMaxConcurrentRequests()),
                globalQueue.getQueueLength(),
                activeCount(authActive, properties.getAuthMaxConcurrentRequests()),
                authQueue.getQueueLength());
    }

    private int activeCount(Semaphore semaphore, int max) {
        return max - semaphore.availablePermits();
    }
}
