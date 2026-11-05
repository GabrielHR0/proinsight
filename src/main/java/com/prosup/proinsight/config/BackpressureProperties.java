package com.prosup.proinsight.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * HTTP admission control configuration.
 *
 * <p>The global limit protects the JVM and MongoDB from aggregate spikes. The
 * authentication limit is separate because login/registration use BCrypt and
 * consume much more CPU than other requests.</p>
 */
@Validated
@ConfigurationProperties(prefix = "app.backpressure")
public class BackpressureProperties {

    private boolean enabled = true;

    @Min(1)
    private int maxConcurrentRequests = 40;

    @Min(0)
    private int queueCapacity = 20;

    @Min(0)
    private long queueTimeoutMs = 1000;

    @Min(1)
    private int authMaxConcurrentRequests = 6;

    @Min(0)
    private int authQueueCapacity = 4;

    @Min(0)
    private long authQueueTimeoutMs = 500;

    @Min(1)
    private int retryAfterSeconds = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public long getQueueTimeoutMs() {
        return queueTimeoutMs;
    }

    public void setQueueTimeoutMs(long queueTimeoutMs) {
        this.queueTimeoutMs = queueTimeoutMs;
    }

    public int getAuthMaxConcurrentRequests() {
        return authMaxConcurrentRequests;
    }

    public void setAuthMaxConcurrentRequests(int authMaxConcurrentRequests) {
        this.authMaxConcurrentRequests = authMaxConcurrentRequests;
    }

    public int getAuthQueueCapacity() {
        return authQueueCapacity;
    }

    public void setAuthQueueCapacity(int authQueueCapacity) {
        this.authQueueCapacity = authQueueCapacity;
    }

    public long getAuthQueueTimeoutMs() {
        return authQueueTimeoutMs;
    }

    public void setAuthQueueTimeoutMs(long authQueueTimeoutMs) {
        this.authQueueTimeoutMs = authQueueTimeoutMs;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public void setRetryAfterSeconds(int retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
