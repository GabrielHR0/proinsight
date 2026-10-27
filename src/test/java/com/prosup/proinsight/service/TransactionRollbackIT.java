package com.prosup.proinsight.service;

import com.prosup.proinsight.AbstractIntegrationTest;
import com.prosup.proinsight.api.dto.request.ClienteComImcRequest;
import com.prosup.proinsight.api.dto.request.RegisterRequest;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.infrastructure.persistence.document.PasswordResetTokenDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.ClienteRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionRollbackIT extends AbstractIntegrationTest {

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    private AcademiaRepository academiaRepository;

    @Test
    @DisplayName("registration rollback: user is not persisted when academia creation fails")
    void registrationRollsBackUserWhenAcademiaCreationFails() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest request = new RegisterRequest();
        request.setEmail("rollback-reg-" + suffix + "@test.com");
        request.setPassword("Test@123");
        request.setUserName("rollback-reg-" + suffix);
        request.setCref("CREF-ROLLBACK-" + suffix);
        request.setCpf("rollback-cpf-" + suffix);
        request.setAcademiaNome("Academia Rollback " + suffix);
        request.setCnpj("rollback-cnpj-" + suffix);

        when(academiaRepository.save(any())).thenThrow(
                new DataIntegrityViolationException("forced academia failure"));

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(userRepository.findByEmail(request.getEmail())).isEmpty();
        verify(academiaRepository).save(any());
    }

    @Test
    @DisplayName("client + IMC rollback: client is not persisted when assessment fails")
    void clientWithImcRollsBackWhenAssessmentFails() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String academiaId = "rollback-academia-" + suffix;

        ClienteComImcRequest request = new ClienteComImcRequest();
        request.setFullName("Cliente Rollback " + suffix);
        request.setEmail("rollback-client-" + suffix + "@test.com");
        request.setPhone("11999999999");
        request.setCpf("52998224725");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setSexo(Sexo.MASCULINO);
        request.setAcademiaId(academiaId);
        request.setAvaliadorId("rollback-avaliador-" + suffix);
        request.setProtocoloId("missing-protocol-" + suffix);
        request.setPesoGramas(75000);
        request.setAlturaCm(175);

        assertThatThrownBy(() -> clienteService.criarComImc(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Protocolo não encontrado");

        assertThat(clienteRepository.findByAcademiaId(academiaId)).isEmpty();
    }

    @Test
    @DisplayName("password reset rollback: token remains unused when user is missing")
    void passwordResetRollsBackTokenWhenUserIsMissing() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String rawToken = "rollback-reset-" + suffix;
        String tokenHash = sha256(rawToken);

        PasswordResetTokenDocument token = passwordResetTokenRepository.save(
                new PasswordResetTokenDocument(
                        tokenHash,
                        "missing-user-" + suffix,
                        Instant.now().plusSeconds(600)));

        try {
            assertThatThrownBy(() -> passwordResetService.resetPassword(rawToken, "NewPass@123"))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Usuário não encontrado");

            PasswordResetTokenDocument reloaded =
                    passwordResetTokenRepository.findById(token.getId()).orElseThrow();
            assertThat(reloaded.isUsed()).isFalse();
        } finally {
            passwordResetTokenRepository.deleteById(token.getId());
        }
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
