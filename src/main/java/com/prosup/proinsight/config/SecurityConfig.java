package com.prosup.proinsight.config;

import com.prosup.proinsight.infrastructure.persistence.repository.ApiKeyRepository;
import com.prosup.proinsight.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final ApiKeyRepository apiKeyRepository;
    private final UserPermissionService userPermissionService;

    public SecurityConfig(JwtTokenProvider tokenProvider,
                          ApiKeyRepository apiKeyRepository,
                          UserPermissionService userPermissionService) {
        this.tokenProvider = tokenProvider;
        this.apiKeyRepository = apiKeyRepository;
        this.userPermissionService = userPermissionService;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userPermissionService);
    }

    @Bean
    public LoginRateLimiterFilter loginRateLimiterFilter() {
        return new LoginRateLimiterFilter();
    }

    @Bean
    public TenantContextFilter tenantContextFilter() {
        return new TenantContextFilter();
    }

    @Bean
    public TenantRateLimiterFilter tenantRateLimiterFilter(
            @Value("${security.tenant-rate-limit.max-per-minute:600}") int maxPerMinute,
            @Value("${security.tenant-rate-limit.enabled:true}") boolean enabled) {
        return new TenantRateLimiterFilter(maxPerMinute, enabled);
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter() {
        return new ApiKeyAuthenticationFilter(apiKeyRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   TenantRateLimiterFilter tenantRateLimiterFilter)
            throws Exception
    {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy
                        (SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(policy -> policy.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .crossOriginOpenerPolicy(policy -> policy.policy(
                                org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'"))
                        .permissionsPolicy(policy -> policy.policy(
                                "camera=(), microphone=(), geolocation=()")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/forgot-password").permitAll()
                        .requestMatchers("/api/v1/auth/reset-password").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/prometheus").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/actuator/metrics", "/actuator/metrics/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/actuator/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(apiKeyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginRateLimiterFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantContextFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantRateLimiterFilter, TenantContextFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws  Exception {
        return authConfig.getAuthenticationManager();
    }

}
