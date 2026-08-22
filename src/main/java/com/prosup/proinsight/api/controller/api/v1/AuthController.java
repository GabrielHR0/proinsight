package com.prosup.proinsight.api.controller.api.v1;

import com.prosup.proinsight.api.annotation.Audited;
import com.prosup.proinsight.api.dto.request.ChangePasswordRequest;
import com.prosup.proinsight.api.dto.request.ForgotPasswordRequest;
import com.prosup.proinsight.api.dto.request.LoginRequest;
import com.prosup.proinsight.api.dto.request.MinhaAcademiaRequest;
import com.prosup.proinsight.api.dto.request.RefreshTokenRequest;
import com.prosup.proinsight.api.dto.request.RegisterRequest;
import com.prosup.proinsight.api.dto.request.ResetPasswordRequest;
import com.prosup.proinsight.api.dto.response.AcademiaResponse;
import com.prosup.proinsight.api.dto.response.LoginResponse;
import com.prosup.proinsight.api.dto.response.MeResponse;
import com.prosup.proinsight.api.handler.RateLimitExceededException;
import com.prosup.proinsight.config.JwtTokenProvider;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.infrastructure.persistence.mapper.UserMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.service.AcademiaService;
import com.prosup.proinsight.service.CustomUserDetailsService;
import com.prosup.proinsight.service.LoginLockoutService;
import com.prosup.proinsight.service.PasswordResetService;
import com.prosup.proinsight.service.RateLimitService;
import com.prosup.proinsight.service.RefreshTokenService;
import com.prosup.proinsight.service.RegistrationService;
import com.prosup.proinsight.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RegistrationService registrationService;
    private final LoginLockoutService loginLockoutService;
    private final AcademiaService academiaService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RateLimitService rateLimitService;
    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;
    private final com.prosup.proinsight.service.UserPermissionService userPermissionService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          RefreshTokenService refreshTokenService,
                          CustomUserDetailsService userDetailsService,
                          UserRepository userRepository,
                          RegistrationService registrationService,
                          LoginLockoutService loginLockoutService,
                          AcademiaService academiaService,
                          TokenBlacklistService tokenBlacklistService,
                          RateLimitService rateLimitService,
                          PasswordResetService passwordResetService,
                          PasswordEncoder passwordEncoder,
                          com.prosup.proinsight.service.UserPermissionService userPermissionService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.loginLockoutService = loginLockoutService;
        this.academiaService = academiaService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.rateLimitService = rateLimitService;
        this.passwordResetService = passwordResetService;
        this.passwordEncoder = passwordEncoder;
        this.userPermissionService = userPermissionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String identityKey = "login:" + request.getLogin().toLowerCase(Locale.ROOT);
        if (rateLimitService.isBlocked(identityKey)) {
            log.warn("Rate limit por identidade excedido no login: {}", request.getLogin());
            throw new RateLimitExceededException("Muitas tentativas para esta conta. Tente novamente em 1 minuto.");
        }

        loginLockoutService.checkLockout(request.getLogin());

        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    request.getLogin(), request.getPassword());
            var authentication = authenticationManager.authenticate(authToken);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            loginLockoutService.resetAttempts(request.getLogin());
            rateLimitService.recordSuccess(identityKey);
            log.info("Login bem-sucedido: {}", request.getLogin());
            return ResponseEntity.ok(buildLoginResponse(userDetails));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            loginLockoutService.recordFailedAttempt(request.getLogin());
            rateLimitService.recordFailure(identityKey);
            log.warn("Tentativa de login inválida: {}", request.getLogin());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciais inválidas"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        var response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        String identityKey = "refresh:" + sha256(request.getRefreshToken());
        if (rateLimitService.isBlocked(identityKey)) {
            log.warn("Rate limit por identidade excedido no refresh");
            throw new RateLimitExceededException("Muitas tentativas. Tente novamente em 1 minuto.");
        }

        try {
            var tokenDoc = refreshTokenService.validateAndRevoke(request.getRefreshToken());

            var userDoc = userRepository.findById(tokenDoc.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            var user = UserMapper.toDomain(userDoc);
            CustomUserDetails userDetails = userDetailsService.toUserDetails(user);
            rateLimitService.recordSuccess(identityKey);
            return ResponseEntity.ok(buildLoginResponse(userDetails));
        } catch (RuntimeException e) {
            rateLimitService.recordFailure(identityKey);
            throw e;
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (CustomUserDetails) authentication.getPrincipal();
        refreshTokenService.revokeAllByUserId(userDetails.getUser().getId());

        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            try {
                String jti = tokenProvider.getJti(bearer.substring(7));
                tokenBlacklistService.revoke(jti, userDetails.getUser().getId(),
                        Instant.now().plusMillis(tokenProvider.getExpirationMs()));
            } catch (RuntimeException e) {
                log.warn("Não foi possível revogar o access token no logout: {}", e.getMessage());
            }
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    @Audited
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               HttpServletRequest httpRequest) {
        var userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        var user = userDetails.getUser();

        var userDoc = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), userDoc.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        userDoc.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDoc.setFailedLoginAttempts(0);
        userDoc.setLockedUntil(null);
        userRepository.save(userDoc);

        refreshTokenService.revokeAllByUserId(user.getId());

        String bearer = httpRequest.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            try {
                String jti = tokenProvider.getJti(bearer.substring(7));
                tokenBlacklistService.revoke(jti, user.getId(),
                        Instant.now().plusMillis(tokenProvider.getExpirationMs()));
            } catch (RuntimeException e) {
                log.warn("Não foi possível revogar o access token na troca de senha: {}", e.getMessage());
            }
        }
        userPermissionService.evict(user.getId());
        log.info("Senha alterada para o usuário: {}", user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String identityKey = "forgot:" + request.getEmail().toLowerCase(Locale.ROOT);
        if (rateLimitService.isBlocked(identityKey)) {
            throw new RateLimitExceededException("Muitas tentativas. Tente novamente em 1 minuto.");
        }

        String rawToken = passwordResetService.createResetToken(request.getEmail());
        rateLimitService.recordSuccess(identityKey);

        if (rawToken == null) {
            log.warn("Forgot-password para e-mail desconhecido: {}", request.getEmail());
            return ResponseEntity.ok().build();
        }

        log.info("Token de reset gerado para {}", request.getEmail());
        return ResponseEntity.ok(Map.of("resetToken", rawToken));
    }

    @PostMapping("/reset-password")
    @Audited
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String identityKey = "reset:" + sha256(request.getToken());
        if (rateLimitService.isBlocked(identityKey)) {
            throw new RateLimitExceededException("Muitas tentativas. Tente novamente em 1 minuto.");
        }

        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            rateLimitService.recordSuccess(identityKey);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            rateLimitService.recordFailure(identityKey);
            throw e;
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeResponse> me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = userDetails.getUser();
        Map<String, List<String>> academiaPermissoes = buildPermissoesMap(userDetails);

        return ResponseEntity.ok(new MeResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                academiaPermissoes,
                user.getAcademiaIds() != null ? user.getAcademiaIds() : Set.of()));
    }

    @GetMapping("/me/academia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> minhaAcademia() {
        var user = getCurrentUser();
        var academia = academiaService.findFirstByOwnerId(user.getId());
        if (academia == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(academia);
    }

    @PutMapping("/me/academia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> atualizarMinhaAcademia(@Valid @RequestBody MinhaAcademiaRequest request) {
        var user = getCurrentUser();
        var academia = academiaService.findFirstByOwnerId(user.getId());
        if (academia == null) {
            return ResponseEntity.notFound().build();
        }
        var updated = academiaService.updateFromSelf(academia.id(), request);
        return ResponseEntity.ok(updated);
    }

    private com.prosup.proinsight.domain.model.User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private LoginResponse buildLoginResponse(CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        Map<String, List<String>> academiaPermissoes = buildPermissoesMap(userDetails);

        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getId();
        String jwt = tokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        return new LoginResponse(
                jwt,
                refreshToken,
                "Bearer",
                tokenProvider.getExpirationMs() / 1000,
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                academiaPermissoes);
    }

    private static Map<String, List<String>> buildPermissoesMap(CustomUserDetails userDetails) {
        Map<String, List<String>> academiaPermissoes = new HashMap<>();
        if (userDetails.getAcademiaPermissoes() != null) {
            for (var entry : userDetails.getAcademiaPermissoes().entrySet()) {
                academiaPermissoes.put(
                        entry.getKey(),
                        entry.getValue().stream().map(Enum::name).toList());
            }
        }
        return academiaPermissoes;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
